package com.cutepet.iam;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Spring Data 仓库集合（编译与运行属开发机 mvn/local-test 职责）。 */
interface Repos {

    interface UserRepo extends JpaRepository<UserEntity, Long> {
        Optional<UserEntity> findByPhone(String phone);
    }

    interface AgreementRepo extends JpaRepository<UserAgreementEntity, Long> {
    }

    interface GuardianRepo extends JpaRepository<GuardianConsentEntity, Long> {
        Optional<GuardianConsentEntity> findByMinorUserId(Long minorUserId);
    }

    interface FamilyRepo extends JpaRepository<FamilyEntity, Long> {
        Optional<FamilyEntity> findByOwnerUserIdAndState(Long ownerUserId, String state);
    }

    interface MemberRepo extends JpaRepository<FamilyMemberEntity, Long> {
        Optional<FamilyMemberEntity> findByUserId(Long userId);

        List<FamilyMemberEntity> findByFamilyId(Long familyId);
    }

    interface InviteRepo extends JpaRepository<FamilyInviteEntity, String> {
        List<FamilyInviteEntity> findByFamilyId(Long familyId);
    }

    interface ShareRepo extends JpaRepository<FamilyShareEntity, Long> {
        List<FamilyShareEntity> findByFamilyId(Long familyId);
    }

    interface TransferRepo extends JpaRepository<OwnershipTransferEntity, Long> {
        List<OwnershipTransferEntity> findByFromUserAndState(Long fromUser, String state);
    }

    interface DeletionRepo extends JpaRepository<AccountDeletionEntity, Long> {
    }

    interface PhoneChangeRepo extends JpaRepository<PhoneChangeEntity, Long> {
    }

    interface ExportRepo extends JpaRepository<PrivacyExportEntity, Long> {
        List<PrivacyExportEntity> findByUserId(Long userId);
    }
}
