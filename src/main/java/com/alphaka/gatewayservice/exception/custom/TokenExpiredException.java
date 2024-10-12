package com.alphaka.gatewayservice.exception.custom;

import com.alphaka.gatewayservice.exception.ErrorCode;

public class TokenExpiredException extends CustomException {

    public TokenExpiredException() {
        super(ErrorCode.TOKEN_EXPIRED);
    }
}
