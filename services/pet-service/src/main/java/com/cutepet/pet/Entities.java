package com.cutepet.pet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** pet 持久化实体（映射 cutepet_pet V1）。 */
@Entity
@Table(name = "pets", schema = "cutepet_pet")
class PetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "owner_user_id", nullable = false)
    public Long ownerUserId;
    public String name;
    public String species;
    public String breed = "";
    public String sex;
    public Boolean neutered;
    public LocalDate birthDate;
    public String photo = "";
    public String state = "ACTIVE"; // ACTIVE/ARCHIVED/DELETED
    @Column(name = "deleted_at")
    public LocalDateTime deletedAt;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}

@Entity
@Table(name = "health_records", schema = "cutepet_pet")
class HealthRecordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "pet_id", nullable = false)
    public Long petId;
    public String kind; // 疫苗/驱虫/体检/就医/用药/过敏
    @Column(name = "event_date", nullable = false)
    public LocalDate eventDate;
    @Column(name = "valid_from")
    public LocalDate validFrom;
    @Column(name = "valid_to")
    public LocalDate validTo;
    public String org;
    public String doctor;
    public String note = "";
    public String photo;
    @Column(name = "created_by", nullable = false)
    public Long createdBy;
    public LocalDateTime createdAt;
}

@Entity
@Table(name = "weight_samples", schema = "cutepet_pet")
class WeightSampleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "pet_id", nullable = false)
    public Long petId;
    @Column(name = "measured_at", nullable = false)
    public LocalDateTime measuredAt;
    @Column(name = "weight_kg", nullable = false)
    public Double weightKg;
    public String note = "";
    @Column(name = "created_by", nullable = false)
    public Long createdBy;
}

@Entity
@Table(name = "reminders", schema = "cutepet_pet")
class ReminderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "pet_id", nullable = false)
    public Long petId;
    public String type;
    public String title = "";
    @Column(name = "recurrence_kind", nullable = false)
    public String recurrenceKind; // once/everyDays/yearly
    @Column(name = "recurrence_rule", nullable = false)
    public String recurrenceRule; // once:2026-10-01 | everyDays:30|2026-09-01 | yearly:2|29|2028-03-01
    public String basis = "plan"; // plan/completion（U55 双基准）
    public String state = "ACTIVE"; // ACTIVE/DONE/EXPIRED/CLOSED
    @Column(name = "next_due")
    public LocalDate nextDue;
    @Column(name = "overdue_reminded")
    public int overdueReminded = 0;
    @Column(name = "created_by", nullable = false)
    public Long createdBy;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}

@Entity
@Table(name = "plans", schema = "cutepet_pet")
class PlanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "pet_id", nullable = false)
    public Long petId;
    public String kind = "PLAN";
    public String title;
    @Column(name = "recurrence_kind", nullable = false)
    public String recurrenceKind;
    @Column(name = "recurrence_rule", nullable = false)
    public String recurrenceRule;
    public String basis = "plan";
    public String state = "ACTIVE"; // ACTIVE/PAUSED/CLOSED
    @Column(name = "created_by", nullable = false)
    public Long createdBy;
    public LocalDateTime createdAt;
}

@Entity
@Table(name = "plan_todos", schema = "cutepet_pet")
class PlanTodoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "plan_id", nullable = false)
    public Long planId;
    @Column(name = "due_date", nullable = false)
    public LocalDate dueDate;
    public String state = "PENDING"; // PENDING/DONE/SKIPPED
    @Column(name = "completed_at")
    public LocalDateTime completedAt;
    @Column(name = "completed_by")
    public Long completedBy;
    @Column(name = "skip_reason")
    public String skipReason;
}
