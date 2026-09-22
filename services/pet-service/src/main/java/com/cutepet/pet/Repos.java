package com.cutepet.pet;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** pet Spring Data 仓库集合。 */
interface Repos {

    interface PetRepo extends JpaRepository<PetEntity, Long> {
        List<PetEntity> findByOwnerUserIdOrderByCreatedAtDesc(Long ownerUserId);

        List<PetEntity> findByOwnerUserIdAndStateOrderByCreatedAtDesc(Long ownerUserId, String state);
    }

    interface HealthRecordRepo extends JpaRepository<HealthRecordEntity, Long> {
        List<HealthRecordEntity> findByPetIdOrderByEventDateDesc(Long petId);
    }

    interface WeightRepo extends JpaRepository<WeightSampleEntity, Long> {
        List<WeightSampleEntity> findByPetIdOrderByMeasuredAtAsc(Long petId);
    }

    interface ReminderRepo extends JpaRepository<ReminderEntity, Long> {
        List<ReminderEntity> findByPetIdAndStateOrderByNextDueAsc(Long petId, String state);

        List<ReminderEntity> findByPetIdOrderByNextDueAsc(Long petId);
    }

    interface PlanRepo extends JpaRepository<PlanEntity, Long> {
        List<PlanEntity> findByPetIdAndStateOrderByCreatedAtDesc(Long petId, String state);

        List<PlanEntity> findAllByPetId(Long petId);
    }

    interface PlanTodoRepo extends JpaRepository<PlanTodoEntity, Long> {
        Optional<PlanTodoEntity> findByPlanIdAndDueDate(Long planId, LocalDate dueDate);

        List<PlanTodoEntity> findByPlanIdOrderByDueDateAsc(Long planId);
    }
}
