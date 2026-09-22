package com.cutepet.pet;

import com.cutepet.common.web.ForbiddenException;
import com.cutepet.common.web.NotFoundException;
import com.cutepet.pet.domain.HealthRecordRules;
import com.cutepet.pet.domain.PetRules;
import com.cutepet.pet.domain.PlanTodoRules;
import com.cutepet.pet.domain.ReminderRules;
import com.cutepet.pet.domain.ReminderRules.Basis;
import com.cutepet.pet.domain.ReminderRules.CompleteOutcome;
import com.cutepet.pet.domain.ReminderRules.Recurrence;
import com.cutepet.pet.domain.ReminderRules.State;
import com.cutepet.pet.domain.SummaryRules;
import com.cutepet.pet.domain.WeightRules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * pet 应用服务（T2.1–T2.6 编排）。判定全部委托 domain（PetLogicTest 70 断言覆盖）。
 * dev shim：身份取 X-User-Id；家庭共享档位联动（iam shares）在本地测试阶段接通。
 */
@Service
class PetService {

    private final Repos.PetRepo pets;

    PetService(Repos.PetRepo pets) {
        this.pets = pets;
    }

    private PetEntity owned(long petId, long userId) {
        PetEntity pet = pets.findById(petId).orElseThrow(() -> new NotFoundException("宠物不存在"));
        if (!pet.ownerUserId.equals(userId)) {
            throw new ForbiddenException("仅所有者可操作（家庭档位联动随本地阶段接通）");
        }
        return pet;
    }

    public List<PetEntity> list(long userId) {
        return pets.findByOwnerUserIdAndStateOrderByCreatedAtDesc(userId, "ACTIVE"); // 归档/软删不进默认列表
    }

    public List<PetEntity> listAll(long userId) {
        return pets.findByOwnerUserIdOrderByCreatedAtDesc(userId);
    }

    public PetEntity get(long petId, long userId) {
        return owned(petId, userId);
    }

    @Transactional
    public PetEntity create(long userId, String name, String species, String breed,
                            String sex, Boolean neutered, LocalDate birthDate) {
        if (!PetRules.validName(name)) {
            throw new IllegalArgumentException("宠物昵称需 1–20 字符");
        }
        PetEntity pet = new PetEntity();
        pet.ownerUserId = userId;
        pet.name = name.trim();
        pet.species = species;
        pet.breed = breed == null ? "" : breed;
        pet.sex = sex;
        pet.neutered = neutered;
        pet.birthDate = birthDate;
        pet.createdAt = LocalDateTime.now();
        pet.updatedAt = LocalDateTime.now();
        return pets.save(pet);
    }

    @Transactional
    public PetEntity update(long petId, long userId, String name, String breed, String sex,
                            Boolean neutered, LocalDate birthDate) {
        PetEntity pet = owned(petId, userId);
        if (name != null) {
            if (!PetRules.validName(name)) {
                throw new IllegalArgumentException("宠物昵称需 1–20 字符");
            }
            pet.name = name.trim();
        }
        if (breed != null) {
            pet.breed = breed;
        }
        if (sex != null) {
            pet.sex = sex;
        }
        if (neutered != null) {
            pet.neutered = neutered;
        }
        if (birthDate != null) {
            pet.birthDate = birthDate;
        }
        pet.updatedAt = LocalDateTime.now();
        return pets.save(pet);
    }

    @Transactional
    public Map<String, Object> transition(long petId, long userId, String target) {
        PetEntity pet = owned(petId, userId);
        if (!PetRules.validStateTarget(target) || !PetRules.canTransition(pet.state, target)) {
            throw new IllegalStateException("状态不可流转: " + pet.state + " → " + target);
        }
        pet.state = target;
        pet.deletedAt = "DELETED".equals(target) ? LocalDateTime.now() : null;
        pet.updatedAt = LocalDateTime.now();
        pets.save(pet);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("petId", petId);
        result.put("state", pet.state);
        if ("DELETED".equals(target)) {
            result.put("recoverableDays", PetRules.SOFT_DELETE_DAYS); // 软删 30 天
        }
        return result;
    }

    @Transactional
    public Map<String, Object> restore(long petId, long userId) {
        PetEntity pet = pets.findById(petId).orElseThrow(() -> new NotFoundException("宠物不存在"));
        if (!pet.ownerUserId.equals(userId)) {
            throw new ForbiddenException("仅所有者可恢复");
        }
        if ("DELETED".equals(pet.state)) {
            if (pet.deletedAt == null || !PetRules.recoverable(pet.deletedAt.toInstant(),
                    java.time.Instant.now())) {
                throw new IllegalStateException("超过 30 天软删窗口，无法普通恢复（U66）");
            }
        } else if (!PetRules.canTransition(pet.state, "ACTIVE")) {
            throw new IllegalStateException("状态不可流转: " + pet.state);
        }
        pet.state = "ACTIVE";
        pet.deletedAt = null;
        pet.updatedAt = LocalDateTime.now();
        pets.save(pet);
        return Map.of("petId", petId, "state", "ACTIVE");
    }
}

@Service
class RecordService {

    private final Repos.PetRepo pets;
    private final Repos.HealthRecordRepo records;
    private final Repos.WeightRepo weights;

    RecordService(Repos.PetRepo pets, Repos.HealthRecordRepo records, Repos.WeightRepo weights) {
        this.pets = pets;
        this.records = records;
        this.weights = weights;
    }

    private PetEntity owned(long petId, long userId) {
        PetEntity pet = pets.findById(petId).orElseThrow(() -> new NotFoundException("宠物不存在"));
        if (!pet.ownerUserId.equals(userId)) {
            throw new ForbiddenException("仅所有者可操作");
        }
        return pet;
    }

    @Transactional
    public Map<String, Object> create(long petId, long userId, String kind, LocalDate eventDate,
                                      LocalDate validFrom, LocalDate validTo, String org, String doctor,
                                      String note, String photo, LocalDate nextReminderDate) {
        owned(petId, userId);
        LocalDate today = LocalDate.now();
        if (!HealthRecordRules.validKind(kind)) {
            throw new IllegalArgumentException("记录类型不合法");
        }
        if (!HealthRecordRules.eventDateValid(eventDate, today)) {
            throw new IllegalArgumentException("事件日期不得晚于今天（U67）");
        }
        if (!HealthRecordRules.medCourseValid(validFrom, validTo)) {
            throw new IllegalArgumentException("用药结束日期不得早于开始日期");
        }
        HealthRecordEntity row = new HealthRecordEntity();
        row.petId = petId;
        row.kind = kind;
        row.eventDate = eventDate;
        row.validFrom = validFrom;
        row.validTo = validTo;
        row.org = org;
        row.doctor = doctor;
        row.note = note == null ? "" : note;
        row.photo = photo;
        row.createdBy = userId;
        row.createdAt = LocalDateTime.now();
        records.save(row);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", row.id);
        // 疫苗/驱虫 + 下次日期 → 自动生成提醒建议（B4 联动 B5；确认落库由 reminders 接口完成）
        HealthRecordRules.suggestedReminder(kind, eventDate, nextReminderDate)
                .ifPresent(s -> result.put("suggestedReminder", Map.of(
                        "type", s.type(), "kind", s.recurrenceKind(), "days", s.days(),
                        "month", s.month(), "day", s.day())));
        return result;
    }

    @Transactional
    public HealthRecordEntity update(long recordId, long userId, String note, LocalDate validTo) {
        HealthRecordEntity row = records.findById(recordId).orElseThrow(() -> new NotFoundException("记录不存在"));
        if (!row.createdBy.equals(userId)) {
            throw new ForbiddenException("仅录入人可编辑");
        }
        if (note != null) {
            row.note = note;
        }
        if (validTo != null && !HealthRecordRules.medCourseValid(row.validFrom, validTo)) {
            throw new IllegalArgumentException("用药结束日期不得早于开始日期");
        }
        row.validTo = validTo != null ? validTo : row.validTo;
        return records.save(row);
    }

    @Transactional
    public Map<String, Object> delete(long recordId, long userId) {
        HealthRecordEntity row = records.findById(recordId).orElseThrow(() -> new NotFoundException("记录不存在"));
        if (!row.createdBy.equals(userId)) {
            throw new ForbiddenException("仅录入人可删除");
        }
        records.delete(row);
        // U87：关联删除不产生第二期排程
        return Map.of("deleted", true, "schedulesCreated", PlanTodoRules.schedulesCreatedOnRecordDelete());
    }

    public List<HealthRecordEntity> listRecords(long petId, long userId) {
        owned(petId, userId);
        return records.findByPetIdOrderByEventDateDesc(petId);
    }

    /** 时间线：健康记录 + 体重条目按时间聚合（B2），支持类型筛选 */
    public List<Map<String, Object>> timeline(long petId, long userId, List<String> filter) {
        owned(petId, userId);
        List<Map<String, Object>> entries = new ArrayList<>();
        for (HealthRecordEntity r : records.findByPetIdOrderByEventDateDesc(petId)) {
            if (HealthRecordRules.timelineVisible(r.kind, filter)) {
                Map<String, Object> e = new LinkedHashMap<>();
                e.put("type", r.kind);
                e.put("date", r.eventDate.toString());
                e.put("recordId", r.id);
                e.put("note", r.note);
                entries.add(e);
            }
        }
        if (filter == null || filter.isEmpty() || filter.contains("体重")) {
            for (WeightSampleEntity w : weights.findByPetIdOrderByMeasuredAtAsc(petId)) {
                Map<String, Object> e = new LinkedHashMap<>();
                e.put("type", "体重");
                e.put("date", w.measuredAt.toLocalDate().toString());
                e.put("recordId", w.id);
                e.put("weightKg", w.weightKg);
                entries.add(e);
            }
        }
        entries.sort((a, b) -> String.valueOf(b.get("date")).compareTo(String.valueOf(a.get("date"))));
        return entries;
    }
}

@Service
class WeightService {

    private final Repos.PetRepo pets;
    private final Repos.WeightRepo weights;

    WeightService(Repos.PetRepo pets, Repos.WeightRepo weights) {
        this.pets = pets;
        this.weights = weights;
    }

    @Transactional
    public Map<String, Object> add(long petId, long userId, double kg, LocalDateTime at, String note) {
        PetEntity pet = pets.findById(petId).orElseThrow(() -> new NotFoundException("宠物不存在"));
        if (!pet.ownerUserId.equals(userId)) {
            throw new ForbiddenException("仅所有者可操作");
        }
        if (!HealthRecordRules.validWeightKg(kg)) {
            throw new IllegalArgumentException("体重需在 0.1–200kg 之间");
        }
        List<WeightRules.Sample> existing = samples(petId);
        WeightRules.Sample prev = WeightRules.latest(existing);

        WeightSampleEntity row = new WeightSampleEntity();
        row.petId = petId;
        row.weightKg = kg;
        row.measuredAt = at == null ? LocalDateTime.now() : at;
        row.note = note == null ? "" : note;
        row.createdBy = userId;
        weights.save(row);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", row.id);
        result.put("needsConfirm", WeightRules.needsConfirm(prev == null ? null : prev.kg(), kg)); // ≥10% 二次确认
        return result;
    }

    private List<WeightRules.Sample> samples(long petId) {
        return weights.findByPetIdOrderByMeasuredAtAsc(petId).stream()
                .map(w -> new WeightRules.Sample(w.measuredAt.toInstant(), w.weightKg))
                .toList();
    }

    public Map<String, Object> curve(long petId, long userId) {
        PetEntity pet = pets.findById(petId).orElseThrow(() -> new NotFoundException("宠物不存在"));
        if (!pet.ownerUserId.equals(userId)) {
            throw new ForbiddenException("仅所有者可操作");
        }
        List<WeightRules.Sample> sorted = WeightRules.sorted(samples(petId));
        java.time.Instant now = java.time.Instant.now();
        WeightRules.Sample base = WeightRules.baseFor(sorted, now);
        WeightRules.Sample latest = WeightRules.latest(sorted);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("points", sorted.stream()
                .map(s -> Map.of("at", s.at().toString(), "kg", s.kg())).toList());
        result.put("hasBase", base != null); // U80：窗口内无基准则不高亮
        if (latest != null) {
            result.put("latestKg", latest.kg());
            result.put("highlighted", WeightRules.highlighted(base, latest.kg()));
            result.put("trend", WeightRules.trend(base, latest.kg()).name());
        }
        if (base != null) {
            result.put("baseAt", base.at().toString());
            result.put("baseKg", base.kg());
        }
        return result;
    }
}

@Service
class ReminderService {

    private static final Logger log = LoggerFactory.getLogger(ReminderService.class);
    private final Repos.PetRepo pets;
    private final Repos.ReminderRepo reminders;

    ReminderService(Repos.PetRepo pets, Repos.ReminderRepo reminders) {
        this.pets = pets;
        this.reminders = reminders;
    }

    private PetEntity owned(long petId, long userId) {
        PetEntity pet = pets.findById(petId).orElseThrow(() -> new NotFoundException("宠物不存在"));
        if (!pet.ownerUserId.equals(userId)) {
            throw new ForbiddenException("仅所有者可操作（家庭可管理档联动随本地阶段接通）");
        }
        return pet;
    }

    static Recurrence parseRecurrence(String kind, String rule) {
        String[] parts = rule.split("\\|");
        return switch (kind) {
            case "once" -> Recurrence.once(LocalDate.parse(parts[0]));
            case "everyDays" -> Recurrence.everyDays(Integer.parseInt(parts[0]), LocalDate.parse(parts[1]));
            case "yearly" -> Recurrence.yearly(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
                    LocalDate.parse(parts[2]));
            default -> throw new IllegalArgumentException("周期类型不合法: " + kind);
        };
    }

    static Basis parseBasis(String basis) {
        return "completion".equals(basis) ? Basis.COMPLETION : Basis.PLAN;
    }

    @Transactional
    public Map<String, Object> create(long petId, long userId, String type, String title, String recurrenceKind,
                                      String recurrenceRule, String basis, String notifyChannels) {
        owned(petId, userId);
        Recurrence recurrence = parseRecurrence(recurrenceKind, recurrenceRule); // 先校验格式
        ReminderEntity row = new ReminderEntity();
        row.petId = petId;
        row.type = type;
        row.title = title == null ? "" : title;
        row.recurrenceKind = recurrenceKind;
        row.recurrenceRule = recurrenceRule;
        row.basis = basis == null ? "plan" : basis;
        row.nextDue = ReminderRules.nextOccurrence(recurrence, LocalDate.now().minusDays(1), parseBasis(row.basis));
        row.createdBy = userId;
        row.createdAt = LocalDateTime.now();
        row.updatedAt = LocalDateTime.now();
        reminders.save(row);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", row.id);
        result.put("nextDue", row.nextDue == null ? null : row.nextDue.toString());
        result.put("channels", notifyChannels == null ? List.of("STATION") : List.of(notifyChannels.split(",")));
        return result;
    }

    public List<ReminderEntity> list(long petId, long userId) {
        owned(petId, userId);
        return reminders.findByPetIdOrderByNextDueAsc(petId);
    }

    @Transactional
    public Map<String, Object> complete(long reminderId, long userId) {
        ReminderEntity row = reminders.findById(reminderId).orElseThrow(() -> new NotFoundException("提醒不存在"));
        Recurrence recurrence = parseRecurrence(row.recurrenceKind, row.recurrenceRule);
        Basis basis = parseBasis(row.basis);
        CompleteOutcome outcome = ReminderRules.complete(State.valueOf(row.state), recurrence, basis, LocalDate.now());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("outcome", outcome.name()); // 幂等：ALREADY_DONE
        if (outcome == CompleteOutcome.DONE_FINAL) {
            row.state = State.DONE.name();
            result.put("final", true);
        } else if (outcome == CompleteOutcome.REARMED_NEXT) {
            row.nextDue = ReminderRules.nextDueAfterComplete(recurrence, basis, LocalDate.now());
            row.state = State.ACTIVE.name(); // 重排下期（唯一期待办由 pet schema 唯一键兜底）
            row.overdueReminded = 0;
            result.put("nextDue", row.nextDue.toString());
        }
        row.updatedAt = LocalDateTime.now();
        reminders.save(row);
        // 调度联动：notification.dispatches 落单在本地阶段接通（reminder+due 唯一键已备）
        log.info("[reminder] complete id={} outcome={}", reminderId, outcome);
        return result;
    }

    @Transactional
    public Map<String, Object> skip(long reminderId, long userId, String reason) {
        if (!ReminderRules.skipValid(reason)) {
            throw new IllegalArgumentException("跳过必须填写原因（U56）");
        }
        ReminderEntity row = reminders.findById(reminderId).orElseThrow(() -> new NotFoundException("提醒不存在"));
        Recurrence recurrence = parseRecurrence(row.recurrenceKind, row.recurrenceRule);
        row.nextDue = ReminderRules.nextOccurrence(recurrence, LocalDate.now(), parseBasis(row.basis));
        row.overdueReminded = 0;
        row.updatedAt = LocalDateTime.now();
        reminders.save(row);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("skipped", true);
        result.put("skipReason", reason);
        result.put("nextDue", row.nextDue == null ? null : row.nextDue.toString());
        return result;
    }

    @Transactional
    public Map<String, Object> close(long reminderId, long userId) {
        ReminderEntity row = reminders.findById(reminderId).orElseThrow(() -> new NotFoundException("提醒不存在"));
        if (!ReminderRules.transition(State.valueOf(row.state), State.CLOSED)) {
            throw new IllegalStateException("状态不可流转: " + row.state);
        }
        row.state = State.CLOSED.name();
        row.updatedAt = LocalDateTime.now();
        reminders.save(row);
        return Map.of("state", "CLOSED");
    }

    @Transactional
    public Map<String, Object> update(long reminderId, long userId, String title, String recurrenceRule) {
        ReminderEntity row = reminders.findById(reminderId).orElseThrow(() -> new NotFoundException("提醒不存在"));
        if (title != null) {
            row.title = title;
        }
        if (recurrenceRule != null) {
            Recurrence recurrence = parseRecurrence(row.recurrenceKind, recurrenceRule); // 校验
            row.recurrenceRule = recurrenceRule;
            row.nextDue = ReminderRules.nextOccurrence(recurrence, LocalDate.now().minusDays(1), parseBasis(row.basis));
        }
        row.updatedAt = LocalDateTime.now();
        reminders.save(row);
        return Map.of("id", row.id, "title", row.title);
    }

    /** 逾期过期标记（与调度联动；3 天二次提醒决议） */
    @Transactional
    public int markOverdue() {
        int count = 0;
        java.time.Instant now = java.time.Instant.now();
        for (ReminderEntity row : reminders.findAll()) {
            if (!"ACTIVE".equals(row.state) || row.nextDue == null) {
                continue;
            }
            if (ReminderRules.overdueExpired(row.nextDue, now, row.overdueReminded == 1)) {
                row.state = State.EXPIRED.name();
                count++;
            } else if (ReminderRules.secondNotifyNeeded(row.nextDue, now, row.overdueReminded == 1)) {
                row.overdueReminded = 1;
                count++;
            }
            reminders.save(row);
        }
        return count;
    }

    public List<Map<String, Object>> pending(long userId) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (PetEntity pet : pets.findByOwnerUserIdAndStateOrderByCreatedAtDesc(userId, "ACTIVE")) {
            for (ReminderEntity r : reminders.findByPetIdAndStateOrderByNextDueAsc(pet.id, "ACTIVE")) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", r.id);
                item.put("petId", r.petId);
                item.put("petName", pet.name);
                item.put("type", r.type);
                item.put("nextDue", r.nextDue == null ? null : r.nextDue.toString());
                out.add(item);
            }
        }
        return out;
    }
}

@Service
class PlanService {

    private final Repos.PetRepo pets;
    private final Repos.PlanRepo plans;
    private final Repos.PlanTodoRepo todos;

    PlanService(Repos.PetRepo pets, Repos.PlanRepo plans, Repos.PlanTodoRepo todos) {
        this.pets = pets;
        this.plans = plans;
        this.todos = todos;
    }

    private PetEntity owned(long petId, long userId) {
        PetEntity pet = pets.findById(petId).orElseThrow(() -> new NotFoundException("宠物不存在"));
        if (!pet.ownerUserId.equals(userId)) {
            throw new ForbiddenException("仅所有者可操作");
        }
        return pet;
    }

    @Transactional
    public Map<String, Object> create(long petId, long userId, String title, String recurrenceKind,
                                      String recurrenceRule, String basis) {
        owned(petId, userId);
        Recurrence recurrence = ReminderService.parseRecurrence(recurrenceKind, recurrenceRule);
        PlanEntity plan = new PlanEntity();
        plan.petId = petId;
        plan.title = title;
        plan.recurrenceKind = recurrenceKind;
        plan.recurrenceRule = recurrenceRule;
        plan.basis = basis == null ? "plan" : basis;
        plan.createdBy = userId;
        plan.createdAt = LocalDateTime.now();
        plan = plans.save(plan);

        // 首期待办（唯一键兜底）
        LocalDate firstDue = ReminderService.parseBasis(plan.basis) == Basis.COMPLETION
                ? LocalDate.now() : ReminderRules.nextOccurrence(recurrence, LocalDate.now().minusDays(1), Basis.PLAN);
        if (firstDue != null && todos.findByPlanIdAndDueDate(plan.id, firstDue).isEmpty()) {
            PlanTodoEntity todo = new PlanTodoEntity();
            todo.planId = plan.id;
            todo.dueDate = firstDue;
            todos.save(todo);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("planId", plan.id);
        result.put("state", plan.state);
        return result;
    }

    public List<PlanEntity> list(long petId, long userId) {
        owned(petId, userId);
        return plans.findAllByPetId(petId);
    }

    @Transactional
    public Map<String, Object> update(long planId, long userId, String state) {
        PlanEntity plan = plans.findById(planId).orElseThrow(() -> new NotFoundException("计划不存在"));
        if (state != null && !PlanTodoRules.planTransition(plan.state, state)) {
            throw new IllegalStateException("计划状态不可流转: " + plan.state + " → " + state);
        }
        plan.state = state == null ? plan.state : state;
        plans.save(plan);
        return Map.of("id", plan.id, "state", plan.state);
    }

    @Transactional
    public Map<String, Object> completeTodo(long todoId, long userId, String completionToken) {
        PlanTodoEntity todo = todos.findById(todoId).orElseThrow(() -> new NotFoundException("待办不存在"));
        PlanEntity plan = plans.findById(todo.planId).orElseThrow(() -> new NotFoundException("计划不存在"));
        Recurrence recurrence = ReminderService.parseRecurrence(plan.recurrenceKind, plan.recurrenceRule);
        Basis basis = ReminderService.parseBasis(plan.basis);

        boolean alreadyDone = "DONE".equals(todo.state);
        java.util.Set<String> keys = new java.util.HashSet<>();
        PlanTodoRules.CompleteResult result = PlanTodoRules.completeOnce(alreadyDone, recurrence, basis,
                todo.dueDate, keys);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("outcome", result.outcome()); // 幂等：ALREADY_COMPLETED（U57 另一人收到已有结果）
        out.put("completionCount", result.completionCount());
        out.put("recordsCreated", result.recordsCreated());
        if ("COMPLETED".equals(result.outcome())) {
            todo.state = "DONE";
            todo.completedAt = LocalDateTime.now();
            todo.completedBy = userId;
            todos.save(todo);
            if (result.nextDue() != null && todos.findByPlanIdAndDueDate(plan.id, result.nextDue()).isEmpty()) {
                PlanTodoEntity next = new PlanTodoEntity();
                next.planId = plan.id;
                next.dueDate = result.nextDue();
                todos.save(next); // 下期恰好 1 个（U57）
            }
            out.put("nextDue", result.nextDue() == null ? null : result.nextDue().toString());
        }
        out.put("completionToken", completionToken); // 幂等令牌透传（重复提交同结果）
        return out;
    }

    public List<PlanTodoEntity> todos(long planId) {
        return todos.findByPlanIdOrderByDueDateAsc(planId);
    }
}

@Service
class SummaryService {

    private final Repos.PetRepo pets;
    private final Repos.HealthRecordRepo records;

    SummaryService(Repos.PetRepo pets, Repos.HealthRecordRepo records) {
        this.pets = pets;
        this.records = records;
    }

    private static final List<String> ALL_MODULES = List.of("档案资料", "健康记录", "提醒计划");

    public Map<String, Object> build(long petId, long userId, List<String> selectedModules) {
        PetEntity pet = pets.findById(petId).orElseThrow(() -> new NotFoundException("宠物不存在"));
        if (!pet.ownerUserId.equals(userId)) {
            throw new ForbiddenException("仅所有者可生成摘要");
        }
        LocalDate today = LocalDate.now();
        List<String> allergiesOld = new ArrayList<>();
        List<String> currentMeds = new ArrayList<>();
        List<String> others = new ArrayList<>();
        for (HealthRecordEntity r : records.findByPetIdOrderByEventDateDesc(petId)) {
            if ("过敏".equals(r.kind) && r.eventDate.isBefore(today.minusDays(30))) {
                allergiesOld.add(String.valueOf(r.id)); // 长期过敏保留（U79）
            } else if ("用药".equals(r.kind) && (r.validTo == null || !r.validTo.isBefore(today))) {
                currentMeds.add(String.valueOf(r.id));   // 当前用药保留（U79）
            } else {
                others.add(String.valueOf(r.id));
            }
        }
        SummaryRules.SummaryView view = SummaryRules.build(
                selectedModules == null ? ALL_MODULES : selectedModules,
                ALL_MODULES, allergiesOld, currentMeds, others);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("pet", Map.of("id", pet.id, "name", pet.name, "species", pet.species,
                "breed", pet.breed == null ? "" : pet.breed));
        out.put("includedModules", view.includedModules());
        out.put("omitted", view.omitted());          // 取消/缺项均可见（U79）
        out.put("recordIds", view.recordIds());
        out.put("phoneExcluded", view.phoneExcluded()); // 隐私：不含主人手机号
        out.put("snapshot", SummaryRules.snapshotNote(today));
        out.put("format", "image-card");             // 生成图片卡由前端渲染（B6）
        return out;
    }
}
