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
        if (exchange.getResponse().getStatusCode() == HttpStatus.NOT_FOUND) {
            log.info("404 Not Found - Request URL: {}, Request Method: {}", exchange.getRequest().getURI(), exchange.getRequest().getMethod().name());
        }
        return Mono.error(ex);
    }
}
