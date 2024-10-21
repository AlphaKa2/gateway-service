package com.alphaka.gatewayservice.filter.global;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 요청 ID 생성 (UUID)
        String requestId = UUID.randomUUID().toString();

        // MDC에 요청 ID 추가
        MDC.put("requestId", requestId);

        // 요청 정보 로그 출력
        ServerHttpRequest request = exchange.getRequest();
        logRequestDetails(request, requestId);

        return chain.filter(exchange).doFinally(signalType -> {
            // 응답 정보 로그 출력
            ServerHttpResponse response = exchange.getResponse();
            logResponseDetails(response, requestId);

            // MDC에서 요청 ID 제거
            MDC.clear();
        });
    }

    private void logRequestDetails(ServerHttpRequest request, String requestId) {
        String method = request.getMethod().name();
        String url = request.getURI().toString();

        // 로그 출력 (요청)
        log.info("Request ID: {} - Method: {}, URL: {}", requestId, method, url);
    }

    private void logResponseDetails(ServerHttpResponse response, String requestId) {
        int statusCode = response.getStatusCode() != null ? response.getStatusCode().value() : 0;

        // 로그 출력 (응답)
        log.info("Request ID: {} - Response Status Code: {}", requestId, statusCode);
    }

    @Override
    public int getOrder() {
        return -1; // 다른 필터들보다 먼저 실행
    }
}
