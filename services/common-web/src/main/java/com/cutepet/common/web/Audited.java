package com.cutepet.common.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 标注在方法上触发审计留痕（需求-管理端 §2.7 / 功能设计-管理端 §8）。 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {
    /** 动作名，如 article.takedown / user.ban */
    String action();

    String targetType() default "";

    String targetIdParam() default "";
}
