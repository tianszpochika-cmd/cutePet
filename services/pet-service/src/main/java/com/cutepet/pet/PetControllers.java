package com.cutepet.pet;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * pet 控制器（契约 /pets/** /records/** /reminders/** /plans/** /plan-todos/**）。
 * dev shim：身份取 X-User-Id。
 */
@RestController
class PetControllers {

    private final PetService petService;
    private final RecordService recordService;
    private final WeightService weightService;
    private final ReminderService reminderService;
    private final PlanService planService;
    private final SummaryService summaryService;

    PetControllers(PetService petService, RecordService recordService, WeightService weightService,
                   ReminderService reminderService, PlanService planService, SummaryService summaryService) {
        this.petService = petService;
        this.recordService = recordService;
        this.weightService = weightService;
        this.reminderService = reminderService;
        this.planService = planService;
        this.summaryService = summaryService;
    }

    private long userOf(String header) {
        if (header == null || header.isBlank()) {
            throw new IllegalArgumentException("缺少用户身份（X-User-Id）");
        }
        return Long.parseLong(header.trim());
    }

    // ---------- 宠物档案（T2.1） ----------

    public record PetCreateReq(String name, String species, String breed, String sex,
                               Boolean neutered, LocalDate birthDate) {
    }

    public record PetPatchReq(String name, String breed, String sex, Boolean neutered, LocalDate birthDate) {
    }

    public record TransitionReq(String target) {
    }

    @GetMapping("/pets")
    public List<PetEntity> listPets(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                    @RequestParam(required = false, defaultValue = "false") boolean all) {
        long userId = userOf(uid);
        return all ? petService.listAll(userId) : petService.list(userId);
    }

    @PostMapping("/pets")
    public PetEntity createPet(@RequestHeader(value = "X-User-Id", required = false) String uid,
                               @RequestBody PetCreateReq req) {
        return petService.create(userOf(uid), req.name(), req.species(), req.breed(),
                req.sex(), req.neutered(), req.birthDate());
    }

    @GetMapping("/pets/{id}")
    public PetEntity getPet(@RequestHeader(value = "X-User-Id", required = false) String uid,
                            @org.springframework.web.bind.annotation.PathVariable long id) {
        return petService.get(id, userOf(uid));
    }

    @PatchMapping("/pets/{id}")
    public PetEntity patchPet(@RequestHeader(value = "X-User-Id", required = false) String uid,
                              @org.springframework.web.bind.annotation.PathVariable long id,
                              @RequestBody PetPatchReq req) {
        return petService.update(id, userOf(uid), req.name(), req.breed(), req.sex(), req.neutered(), req.birthDate());
    }

    @PostMapping("/pets/{id}/archive")
    public Map<String, Object> archivePet(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                          @org.springframework.web.bind.annotation.PathVariable long id) {
        return petService.transition(id, userOf(uid), "ARCHIVED");
    }

    @DeleteMapping("/pets/{id}")
    public Map<String, Object> deletePet(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                         @org.springframework.web.bind.annotation.PathVariable long id) {
        return petService.transition(id, userOf(uid), "DELETED"); // 软删 30 天（二次确认由前端执行）
    }

    @PostMapping("/pets/{id}/restore")
    public Map<String, Object> restorePet(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                          @org.springframework.web.bind.annotation.PathVariable long id) {
        return petService.restore(id, userOf(uid));
    }

    // ---------- 记录与时间线（T2.2） ----------

    public record RecordCreateReq(String kind, LocalDate eventDate, LocalDate validFrom, LocalDate validTo,
                                  String org, String doctor, String note, String photo,
                                  LocalDate nextReminderDate) {
    }

    public record RecordPatchReq(String note, LocalDate validTo) {
    }

    @GetMapping("/pets/{id}/records")
    public List<HealthRecordEntity> listRecords(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                                @org.springframework.web.bind.annotation.PathVariable long id) {
        return recordService.listRecords(id, userOf(uid));
    }

    @PostMapping("/pets/{id}/records")
    public Map<String, Object> createRecord(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                            @org.springframework.web.bind.annotation.PathVariable long id,
                                            @RequestBody RecordCreateReq req) {
        return recordService.create(id, userOf(uid), req.kind(), req.eventDate(), req.validFrom(), req.validTo(),
                req.org(), req.doctor(), req.note(), req.photo(), req.nextReminderDate());
    }

    @PatchMapping("/records/{id}")
    public HealthRecordEntity patchRecord(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                          @org.springframework.web.bind.annotation.PathVariable long id,
                                          @RequestBody RecordPatchReq req) {
        return recordService.update(id, userOf(uid), req.note(), req.validTo());
    }

    @DeleteMapping("/records/{id}")
    public Map<String, Object> deleteRecord(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                            @org.springframework.web.bind.annotation.PathVariable long id) {
        return recordService.delete(id, userOf(uid));
    }

    @GetMapping("/pets/{id}/timeline")
    public List<Map<String, Object>> timeline(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                              @org.springframework.web.bind.annotation.PathVariable long id,
                                              @RequestParam(required = false) List<String> filter) {
        return recordService.timeline(id, userOf(uid), filter);
    }

    // ---------- 体重（T2.3） ----------

    public record WeightAddReq(Double kg, LocalDateTime at, String note) {
    }

    @GetMapping("/pets/{id}/weights")
    public Map<String, Object> weights(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                       @org.springframework.web.bind.annotation.PathVariable long id) {
        return weightService.curve(id, userOf(uid));
    }

    @PostMapping("/pets/{id}/weights")
    public Map<String, Object> addWeight(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                         @org.springframework.web.bind.annotation.PathVariable long id,
                                         @RequestBody WeightAddReq req) {
        if (req.kg() == null) {
            throw new IllegalArgumentException("体重必填");
        }
        return weightService.add(id, userOf(uid), req.kg(), req.at(), req.note());
    }

    // ---------- 提醒（T2.4） ----------

    public record ReminderCreateReq(String type, String title, String recurrenceKind, String recurrenceRule,
                                    String basis, String notifyChannels) {
    }

    public record ReminderPatchReq(String title, String recurrenceRule) {
    }

    public record SkipReq(String reason) {
    }

    @GetMapping("/pets/{id}/reminders")
    public List<ReminderEntity> listReminders(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                              @org.springframework.web.bind.annotation.PathVariable long id) {
        return reminderService.list(id, userOf(uid));
    }

    @PostMapping("/pets/{id}/reminders")
    public Map<String, Object> createReminder(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                              @org.springframework.web.bind.annotation.PathVariable long id,
                                              @RequestBody ReminderCreateReq req) {
        return reminderService.create(id, userOf(uid), req.type(), req.title(), req.recurrenceKind(),
                req.recurrenceRule(), req.basis(), req.notifyChannels());
    }

    @PatchMapping("/reminders/{id}")
    public Map<String, Object> patchReminder(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                             @org.springframework.web.bind.annotation.PathVariable long id,
                                             @RequestBody ReminderPatchReq req) {
        return reminderService.update(id, userOf(uid), req.title(), req.recurrenceRule());
    }

    @PostMapping("/reminders/{id}/complete")
    public Map<String, Object> completeReminder(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                                @org.springframework.web.bind.annotation.PathVariable long id) {
        return reminderService.complete(id, userOf(uid));
    }

    @PostMapping("/reminders/{id}/skip")
    public Map<String, Object> skipReminder(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                            @org.springframework.web.bind.annotation.PathVariable long id,
                                            @RequestBody SkipReq req) {
        return reminderService.skip(id, userOf(uid), req.reason());
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/reminders/{id}")
    public Map<String, Object> closeReminder(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                             @org.springframework.web.bind.annotation.PathVariable long id) {
        return reminderService.close(id, userOf(uid));
    }

    @GetMapping("/reminders/pending")
    public List<Map<String, Object>> pendingReminders(
            @RequestHeader(value = "X-User-Id", required = false) String uid) {
        return reminderService.pending(userOf(uid));
    }

    // ---------- 计划/待办（T2.6） ----------

    public record PlanCreateReq(String title, String recurrenceKind, String recurrenceRule, String basis) {
    }

    public record PlanStateReq(String state) {
    }

    public record TodoCompleteReq(String completionToken) {
    }

    @GetMapping("/pets/{id}/plans")
    public List<PlanEntity> listPlans(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                      @org.springframework.web.bind.annotation.PathVariable long id) {
        return planService.list(id, userOf(uid));
    }

    @PostMapping("/pets/{id}/plans")
    public Map<String, Object> createPlan(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                          @org.springframework.web.bind.annotation.PathVariable long id,
                                          @RequestBody PlanCreateReq req) {
        return planService.create(id, userOf(uid), req.title(), req.recurrenceKind(),
                req.recurrenceRule(), req.basis());
    }

    @PatchMapping("/plans/{id}")
    public Map<String, Object> patchPlan(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                         @org.springframework.web.bind.annotation.PathVariable long id,
                                         @RequestBody PlanStateReq req) {
        return planService.update(id, userOf(uid), req.state());
    }

    @PostMapping("/plan-todos/{id}/complete")
    public Map<String, Object> completeTodo(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                            @org.springframework.web.bind.annotation.PathVariable long id,
                                            @RequestBody(required = false) TodoCompleteReq req) {
        return planService.completeTodo(id, userOf(uid), req == null ? null : req.completionToken());
    }

    // ---------- 健康摘要（T2.5） ----------

    public record SummaryReq(List<String> modules) {
    }

    @PostMapping("/pets/{id}/summary/preview")
    public Map<String, Object> summaryPreview(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                              @org.springframework.web.bind.annotation.PathVariable long id,
                                              @RequestBody(required = false) SummaryReq req) {
        return summaryService.build(id, userOf(uid), req == null ? null : req.modules());
    }

    @GetMapping("/pets/{id}/summary")
    public Map<String, Object> summary(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                       @org.springframework.web.bind.annotation.PathVariable long id) {
        return summaryService.build(id, userOf(uid), null);
    }
}
