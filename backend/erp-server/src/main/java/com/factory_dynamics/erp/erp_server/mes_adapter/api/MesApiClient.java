package com.factory_dynamics.erp.erp_server.mes_adapter.api;

// MesApiClient.java
import com.factory_dynamics.erp.erp_server.mes_adapter.dto.ProductionPlanDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class MesApiClient {

    private final WebClient webClient;

    public MesApiClient(        @Value("${mes.api.base-url}") String mesApiBaseUrl,
                                @Value("${mes.api.username}") String mesUsername, // 설정 파일에서 읽어옴
                                @Value("${mes.api.password}") String mesPassword  // 설정 파일에서 읽어옴
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(mesApiBaseUrl)
                // 🚨 Basic Auth 헤더 추가
                .defaultHeaders(header -> header.setBasicAuth(mesUsername, mesPassword))
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * MES에 생산 계획을 전송합니다.
     * @param planDto 전송할 생산 계획 DTO
     * @return MES 응답 결과 (성공 시 'Success' 메시지, 실패 시 Exception)
     */
    public Mono<String> sendProductionPlan(ProductionPlanDto planDto) {
        // MES의 생산 계획 접수 API: POST /api/mes/production-plans
        return webClient.post()
                .uri("/api/mes/production-plans")
                .body(BodyInserters.fromValue(planDto))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new MesIntegrationException("MES 연동 실패: " + errorBody))))
                .bodyToMono(String.class) // MES에서 응답으로 간단한 성공 메시지를 보낸다고 가정
                .onErrorResume(e -> {
                    // 외부 API 호출 실패 처리
                    System.err.println("MES API 호출 중 오류 발생: " + e.getMessage());
                    return Mono.error(new MesIntegrationException("MES 전송 실패: " + e.getMessage()));
                });
    }

    // 예외 클래스 (필요 시 정의)
    public static class MesIntegrationException extends RuntimeException {
        public MesIntegrationException(String message) {
            super(message);
        }
    }
}