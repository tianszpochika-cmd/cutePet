package com.cutepet.catalog;

import com.cutepet.common.web.ApiExceptionHandler;
import com.cutepet.common.web.AuditAspect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({ApiExceptionHandler.class, AuditAspect.class})
public class CatalogApplication {
    public static void main(String[] args) {
        SpringApplication.run(CatalogApplication.class, args);
    }
}
