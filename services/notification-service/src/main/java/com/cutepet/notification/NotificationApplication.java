package com.cutepet.notification;

import com.cutepet.common.web.ApiExceptionHandler;
import com.cutepet.common.web.AuditAspect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // 提醒调度：每分钟扫描（功能设计-宠物管理 §5）
@Import({ApiExceptionHandler.class, AuditAspect.class})
public class NotificationApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationApplication.class, args);
    }
}
