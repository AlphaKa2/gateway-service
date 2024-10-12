package com.alphaka.gatewayservice.filter;

import com.alphaka.gatewayservice.exception.custom.TokenExpiredException;
import com.alphaka.gatewayservice.jwt.JwtService;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    public static final String AUTHENTICATED_USER_ID_HEADER = "X-User-Id";
    public static final String AUTHENTICATED_USER_ROLE_HEADER = "X-User-Role";
    public static final String AUTHENTICATED_USER_PROFILE_HEADER = "X-User-Profile";
    public static final String AUTHENTICATED_USER_NICKNAME_HEADER = "X-User-Nickname";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        super(Config.class);
        this.jwtService = jwtService;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            Optional<String> maybeAccessToken = jwtService.extractAccessToken(exchange.getRequest());

            //accessToken이 없으면 login 페이지로 리다이렉션
            if (maybeAccessToken.isEmpty()) {
                return handleRedirect(exchange, config.loginPageRedirectionUrl);
            }

            String accessToken = maybeAccessToken.get();
            if (!jwtService.isValidToken(accessToken)) { //유효하지 않다면 401 응답, 프론트는 인증 서비스에 재발급 요청
                throw new TokenExpiredException();
            }

            //유효한 토큰이라면, 유저 정보를 요청 헤더에 추가
            return chain.filter(setAuthenticationHeader(exchange, jwtService.extractUserInformation(accessToken)));
        };
    }

    private ServerWebExchange setAuthenticationHeader(ServerWebExchange exchange, Map<String, String> userInformation) {
        return exchange.mutate()
                .request(r -> r.headers(headers -> {
                    headers.add(AUTHENTICATED_USER_ID_HEADER, userInformation.get(JwtService.ID_CLAIM));
                    headers.add(AUTHENTICATED_USER_ROLE_HEADER, userInformation.get(JwtService.ROLE_CLAIM));
                    headers.add(AUTHENTICATED_USER_NICKNAME_HEADER, userInformation.get(JwtService.NICKNAME_CLAIM));
                    headers.add(AUTHENTICATED_USER_PROFILE_HEADER, userInformation.get(JwtService.PROFILE_CLAIM));
                }))
                .build();
    }

    private Mono<Void> handleRedirect(ServerWebExchange exchange, String redirectionUrl) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.SEE_OTHER);
        response.getHeaders().set("Location", redirectionUrl);
        return exchange.getResponse().setComplete();
    }

    @Getter
    @Setter
    @RequiredArgsConstructor
    static class Config {
        private String loginPageRedirectionUrl;
    }
}

