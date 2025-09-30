package com.globalmed.mes.mes_api.integration.erp;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties(ErpProps.class)
public class RestClientConfig {

    @Bean
    RestClient erpRestClient(ErpProps props, RestClient.Builder builder,
                             @Value("${erp.base-url}") String baseUrl) {
        var jdk = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        return RestClient.builder()
                .baseUrl(props.baseUrl())
                .defaultHeader("X-Caller", "MES") // 식별용
                .requestFactory(new JdkClientHttpRequestFactory(jdk)) // ← JDK로 교체
                .requestInterceptor((req, body, exec) -> {
                    req.getHeaders().add("X-API-Key", props.apiKey());
                    exec.execute(req, body);
                    return exec.execute(req, body);
                })
                .build();
    }
}