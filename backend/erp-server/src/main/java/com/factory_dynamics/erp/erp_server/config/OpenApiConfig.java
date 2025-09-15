package com.factory_dynamics.erp.erp_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI baseOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ERP MVP API")
                        .version("v1")
                        .description("BOM, 생산계획, 원가 계산 MVP API"));
    }
}