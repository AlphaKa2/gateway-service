package com.alphaka.gatewayservice.exception;

public class TokenExpiredException extends CustomException {

    public TokenExpiredException() {
        super(ErrorCode.TOKEN_EXPIRED);
    }
}
