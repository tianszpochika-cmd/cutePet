package com.cutepet.explore;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** explore Spring Data 仓库集合。 */
interface Repos {

    interface PoiRepo extends JpaRepository<PoiEntity, Long> {
        List<PoiEntity> findByState(String state);

        List<PoiEntity> findByCityAndState(String city, String state);

        List<PoiEntity> findByTypeAndState(String type, String state);
    }

    interface PoiPhotoRepo extends JpaRepository<PoiPhotoEntity, Long> {
        List<PoiPhotoEntity> findByPoiIdOrderByOrdAsc(Long poiId);
    }

    interface ReviewRepo extends JpaRepository<PoiReviewEntity, Long> {
        long countByPoiIdAndUserIdAndDayKey(Long poiId, Long userId, String dayKey);

        long countByDeviceKeyAndDayKeyEndingWith(String deviceKey, String dateSuffix);

        List<PoiReviewEntity> findByPoiIdOrderByCreatedAtDesc(Long poiId);

        List<PoiReviewEntity> findByPoiIdAndState(String poiId, String state);

        Optional<PoiReviewEntity> findByPoiIdAndUserIdAndDayKey(Long poiId, Long userId, String dayKey);
    }

    interface CorrectionRepo extends JpaRepository<CorrectionEntity, Long> {
        List<CorrectionEntity> findByPoiIdAndStateAndFieldOrderByIdAsc(Long poiId, String state, String field);

        List<CorrectionEntity> findByStateOrderByIdAsc(String state);

        List<CorrectionEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
    }

    interface RouteRepo extends JpaRepository<UgcRouteEntity, Long> {
        List<UgcRouteEntity> findByCityAndState(String city, String state);

        List<UgcRouteEntity> findByStateOrderByCreatedAtDesc(String state);

        List<UgcRouteEntity> findByUserId(Long userId);
    }

    interface ActivityRepo extends JpaRepository<ActivityEntity, Long> {
        List<ActivityEntity> findByCityAndState(String city, String state);

        List<ActivityEntity> findByStateOrderByBeginsAtAsc(String state);

        List<ActivityEntity> findByOrgUserId(Long orgUserId);
    }

    interface SignupRepo extends JpaRepository<ActivitySignupEntity, Long> {
        long countByActivityIdAndState(Long activityId, String state);

        boolean existsByActivityIdAndUserIdAndState(Long activityId, Long userId, String state);

        List<ActivitySignupEntity> findByActivityIdAndState(Long activityId, String state);

        List<ActivitySignupEntity> findByUserId(Long userId);
    }

    interface AdoptionRepo extends JpaRepository<AdoptionEntity, Long> {
        List<AdoptionEntity> findByCity(String city);

        List<AdoptionEntity> findAllByOrderByCreatedAtDesc();
    }

    interface FavoritePoiRepo extends JpaRepository<FavoritePoiEntity, FavoritePoiKey> {
    }

    interface FavoriteRouteRepo extends JpaRepository<FavoriteRouteEntity, FavoriteRouteKey> {
    }

    interface OrgCredentialRepo extends JpaRepository<OrgCredentialEntity, Long> {
        Optional<OrgCredentialEntity> findByUserId(Long userId);
    }
}
