package com.alphaka.gatewayservice.openfeign;

import com.alphaka.gatewayservice.dto.request.AccessTokenRequest;
import com.alphaka.gatewayservice.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;

@ReactiveFeignClient(name = "auth-service")
public interface BlacklistClient {

    @PostMapping("/blacklist")
    Mono<ApiResponse<Boolean>> blacklist(@RequestBody AccessTokenRequest accessTokenRequest);
}
