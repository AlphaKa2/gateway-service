package com.alphaka.gatewayservice.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@Order(-2)
public class GlobalNotFoundExceptionHandler implements WebExceptionHandler {


    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        // 로그에 예외 메시지와 스택 트레이스를 함께 출력
        log.error("Exception occurred - Request URL: {}, Request Method: {}, Exception: {}",
                exchange.getRequest().getURI(),
                exchange.getRequest().getMethod(),
                ex.toString(), // 예외의 간단한 정보 출력 (메시지 및 클래스명)
                ex); // 스택 트레이스 출력

        return exchange.getResponse().setComplete();
    }
}
