package com.globalmed.mes.mes_api.config;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UtcJacksonConfig {
    @Bean
    public JavaTimeModule javaTimeModule() {
        return new JavaTimeModule(); // ISO 문자열 직렬화
    }
}