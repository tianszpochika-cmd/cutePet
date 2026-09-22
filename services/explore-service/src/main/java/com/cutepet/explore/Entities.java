package com.cutepet.explore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** explore 持久化实体（V1 + V2 nav_clicks + V3 org_credentials）。 */
@Entity
@Table(name = "pois", schema = "cutepet_explore")
class PoiEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String name;
    public String type;
    public String city;
    public Double lng;
    public Double lat;
    public String address = "";
    public String phone = "";
    @Column(name = "open_hours")
    public String openHours = "";
    public String attrs = "";
    public String state = "NORMAL"; // NORMAL/CLOSED
    @Column(name = "avg_score")
    public Double avgScore = 0.0;
    @Column(name = "review_count")
    public int reviewCount = 0;
    @Column(name = "nav_clicks")
    public int navClicks = 0;
    public LocalDateTime createdAt;
}

@Entity
@Table(name = "poi_photos", schema = "cutepet_explore")
class PoiPhotoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "poi_id", nullable = false)
    public Long poiId;
    public String url;
    public int ord = 0;
}

@Entity
@Table(name = "poi_reviews", schema = "cutepet_explore")
class PoiReviewEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "poi_id", nullable = false)
    public Long poiId;
    @Column(name = "user_id", nullable = false)
    public Long userId;
    @Column(name = "score_friendly", nullable = false)
    public int scoreFriendly;
    @Column(name = "score_env", nullable = false)
    public int scoreEnv;
    @Column(name = "score_service", nullable = false)
    public int scoreService;
    public String content = "";
    public String pics;
    public String state = "VISIBLE"; // VISIBLE/PENDING/SELF_VISIBLE/HIDDEN
    @Column(name = "device_key")
    public String deviceKey;
    @Column(name = "day_key", nullable = false)
    public String dayKey; // user#poi#date（DB 唯一键兜底）
    public LocalDateTime createdAt;
}

@Entity
@Table(name = "corrections", schema = "cutepet_explore")
class CorrectionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "poi_id", nullable = false)
    public Long poiId;
    @Column(name = "user_id", nullable = false)
    public Long userId;
    public String field;
    public String proposed;
    public String evidence;
    public String state = "OPEN"; // OPEN/ACCEPTED/REJECTED
    @Column(name = "merged_into")
    public Long mergedInto;
    @Column(name = "handler_id")
    public Long handlerId;
    public String note;
    public LocalDateTime createdAt;
}

@Entity
@Table(name = "ugc_routes", schema = "cutepet_explore")
class UgcRouteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "user_id", nullable = false)
    public Long userId;
    public String city;
    public String name;
    public String points;
    @Column(name = "distance_m")
    public int distanceM;
    @Column(name = "duration_min")
    public int durationMin;
    public String difficulty = "简单";
    public String note = "";
    public String pics;
    public String state = "PENDING"; // PENDING/PUBLISHED/REJECTED（先审后发）
    @Column(name = "reviewer_id")
    public Long reviewerId;
    @Column(name = "review_note")
    public String reviewNote;
    public LocalDateTime createdAt;
}

@Entity
@Table(name = "activities", schema = "cutepet_explore")
class ActivityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "org_user_id", nullable = false)
    public Long orgUserId;
    public String city;
    public String title;
    public String type;
    @Column(name = "begins_at", nullable = false)
    public LocalDateTime beginsAt;
    @Column(name = "ends_at", nullable = false)
    public LocalDateTime endsAt;
    public String address;
    public Double lng;
    public Double lat;
    public int quota = 0;
    @Column(name = "signup_deadline")
    public LocalDateTime signupDeadline;
    public String state = "PENDING"; // PENDING/PUBLISHED/CHANGED/ENDED/CANCELLED
    @Column(name = "change_note")
    public String changeNote;
    public String contact = "";
    public LocalDateTime createdAt;
}

@Entity
@Table(name = "activity_signups", schema = "cutepet_explore")
class ActivitySignupEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "activity_id", nullable = false)
    public Long activityId;
    @Column(name = "user_id", nullable = false)
    public Long userId;
    public String name;
    public String phone;
    public String state = "ACTIVE"; // ACTIVE/CANCELLED/ACTIVITY_CANCELLED
    @Column(name = "consent_version", nullable = false)
    public String consentVersion;
    @Column(name = "consent_at", nullable = false)
    public String consentAt;
    public LocalDateTime createdAt;
}

@Entity
@Table(name = "adoptions", schema = "cutepet_explore")
class AdoptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "org_user_id", nullable = false)
    public Long orgUserId;
    public String city;
    public String title;
    public String content;
    public String contact;
    public String pics;
    @Column(name = "expire_on")
    public LocalDate expireOn;
    public String state = "PENDING"; // PENDING/PUBLISHED/ENDED/REMOVED
    public LocalDateTime createdAt;
}

final class FavoritePoiKey implements Serializable {
    Long userId;
    Long poiId;

    FavoritePoiKey() {
    }

    FavoritePoiKey(Long userId, Long poiId) {
        this.userId = userId;
        this.poiId = poiId;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof FavoritePoiKey k && userId.equals(k.userId) && poiId.equals(k.poiId);
    }

    @Override
    public int hashCode() {
        return userId.hashCode() ^ poiId.hashCode();
    }
}

@Entity
@Table(name = "favorite_pois", schema = "cutepet_explore")
@IdClass(FavoritePoiKey.class)
class FavoritePoiEntity {
    @Id
    @Column(name = "user_id")
    public Long userId;
    @Id
    @Column(name = "poi_id")
    public Long poiId;
    public LocalDateTime createdAt;
}

final class FavoriteRouteKey implements Serializable {
    Long userId;
    Long routeId;

    FavoriteRouteKey() {
    }

    FavoriteRouteKey(Long userId, Long routeId) {
        this.userId = userId;
        this.routeId = routeId;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof FavoriteRouteKey k && userId.equals(k.userId) && routeId.equals(k.routeId);
    }

    @Override
    public int hashCode() {
        return userId.hashCode() ^ routeId.hashCode();
    }
}

@Entity
@Table(name = "favorite_routes", schema = "cutepet_explore")
@IdClass(FavoriteRouteKey.class)
class FavoriteRouteEntity {
    @Id
    @Column(name = "user_id")
    public Long userId;
    @Id
    @Column(name = "route_id")
    public Long routeId;
    public LocalDateTime createdAt;
}

@Entity
@Table(name = "org_credentials", schema = "cutepet_explore")
class OrgCredentialEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "user_id", nullable = false, unique = true)
    public Long userId;
    public String state = "NONE";
    @Column(name = "material_path")
    public String materialPath = "";
    @Column(name = "expires_at")
    public LocalDate expiresAt;
    @Column(name = "reviewed_by")
    public Long reviewedBy;
    public String note;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}
