package com.cutepet.iam.domain;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.Set;

/**
 * 账号域纯规则（T1.1 注册登录 / T1.2 年龄分支 / T1.3 换绑 / 监护人）。
 * 追溯：需求-账号 A1、BR-09、U83/U84、待定项决议（方案甲、dev 验证码 123456）。
 */
public final class AccountRules {

    public static final String DEV_SMS_CODE = "123456";
    public static final int GUARDIAN_AGE = 14;
    private static final String CN_PHONE = "^1[3-9]\\d{9}$";

    private AccountRules() {
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches(CN_PHONE);
    }

    public static String maskPhone(String phone) {
        if (!isValidPhone(phone)) {
            return "***";
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /** dev 模式验证码（真服务属 E02，本地仅逻辑） */
    public static boolean verifyDevSms(String input) {
        return DEV_SMS_CODE.equals(input);
    }

    /** 实际周岁（BR-09/U83：按真实生日比较，非年份差） */
    public static int ageAt(LocalDate birthDate, LocalDate today) {
        if (birthDate == null || today == null || birthDate.isAfter(today)) {
            throw new IllegalArgumentException("出生日期不合法");
        }
        return Period.between(birthDate, today).getYears();
    }

    /** 方案甲：≥14 ADULT 自主注册；<14 MINOR 走监护人流程 */
    public static String ageBand(LocalDate birthDate, LocalDate today) {
        return ageAt(birthDate, today) >= GUARDIAN_AGE ? "ADULT" : "MINOR";
    }

    public static boolean requiresGuardian(String ageBand) {
        return "MINOR".equals(ageBand);
    }

    /** 协议勾选完整性：terms 与 privacy 均需版本记录（默认不勾选由前端保证，此处校验提交） */
    public static boolean agreementsComplete(String termsVersion, String privacyVersion) {
        return termsVersion != null && !termsVersion.isBlank()
                && privacyVersion != null && !privacyVersion.isBlank();
    }

    /** U83：微信登录不得绕过手机号/协议/监护人流程 */
    public static boolean wechatLoginEligible(boolean phoneBound, String smsVerifiedCode,
                                               String ageBand, String terms, String privacy) {
        boolean smsOk = verifyDevSms(smsVerifiedCode);
        boolean guardianOk = !requiresGuardian(ageBand);
        return phoneBound && smsOk && agreementsComplete(terms, privacy) && guardianOk;
    }

    /** T1.3 换绑：原号验证通过、新号合法且不同（记录脱敏旧号） */
    public static boolean canBindPhone(String currentPhone, String oldPhoneInput, String newPhone) {
        return currentPhone != null
                && currentPhone.equals(oldPhoneInput)
                && isValidPhone(newPhone)
                && !currentPhone.equals(newPhone);
    }

    /** U84：监护人仅凭监护号操作同意，不进入儿童账号会话 */
    public static boolean guardianCanOperate(String providedGuardianPhone, String consentGuardianPhone,
                                             String consentState) {
        if (consentState == null || "REVOKED".equals(consentState)) {
            return false;
        }
        return providedGuardianPhone != null && providedGuardianPhone.equals(consentGuardianPhone);
    }

    /** U84：撤回 → 限制儿童会话与推送，不暴露儿童资料 */
    public static Set<String> effectsAfterGuardianRevoke() {
        return Set.of("REVOKE_MINOR_SESSIONS", "DISABLE_PUSH", "NO_PROFILE_EXPOSURE");
    }

    /** 状态机：监护人同意 PENDING → CONSENTED →（撤回）REVOKED */
    public static boolean guardianTransition(String from, String to) {
        return switch (from) {
            case "PENDING" -> "CONSENTED".equals(to);
            case "CONSENTED" -> "REVOKED".equals(to);
            default -> false;
        };
    }
}
