package com.alphaka.gatewayservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED.value(), "USR016", "유효하지 않은 토큰입니다.");

    private final int status;
    private final String code;
    private final String message;
}
