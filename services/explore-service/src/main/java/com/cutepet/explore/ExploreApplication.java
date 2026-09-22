package com.cutepet.explore;

import com.cutepet.common.web.ApiExceptionHandler;
import com.cutepet.common.web.AuditAspect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // U86 报名保留期每日清理
@Import({ApiExceptionHandler.class, AuditAspect.class})
public class ExploreApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExploreApplication.class, args);
    }
}
