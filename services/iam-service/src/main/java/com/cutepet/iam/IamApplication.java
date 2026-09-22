package com.cutepet.iam;

import com.cutepet.common.web.ApiExceptionHandler;
import com.cutepet.common.web.AuditAspect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({ApiExceptionHandler.class, AuditAspect.class}) // common-web 不在扫描路径，显式导入
public class IamApplication {
    public static void main(String[] args) {
        SpringApplication.run(IamApplication.class, args);
    }
}
