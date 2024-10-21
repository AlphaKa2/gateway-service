package com.alphaka.gatewayservice.filter;

import com.alphaka.gatewayservice.dto.request.AccessTokenRequest;
import com.alphaka.gatewayservice.exception.custom.TokenExpiredException;
import com.alphaka.gatewayservice.jwt.JwtService;
import com.alphaka.gatewayservice.openfeign.BlacklistClient;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Slf4j
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    public static final String AUTHENTICATED_USER_ID_HEADER = "X-User-Id";
    public static final String AUTHENTICATED_USER_ROLE_HEADER = "X-User-Role";
    public static final String AUTHENTICATED_USER_PROFILE_HEADER = "X-User-Profile";
    public static final String AUTHENTICATED_USER_NICKNAME_HEADER = "X-User-Nickname";

    private final JwtService jwtService;

    private final BlacklistClient blacklistClient;

    public JwtAuthenticationFilter(JwtService jwtService, @Lazy BlacklistClient blacklistClient) {
        super(Config.class);
        this.jwtService = jwtService;
        this.blacklistClient = blacklistClient;

    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            Optional<String> maybeAccessToken = jwtService.extractAccessToken(exchange.getRequest());
            log.info("사용자 인증 필터 시작");

            //accessToken이 없으면 login 페이지로 리다이렉션
            if (maybeAccessToken.isEmpty()) {
                log.info("accessToken 부재, 로그인 페이지 리다이렉션");
                return handleRedirect(exchange, config.loginPageRedirectionUrl);
            }

            String accessToken = maybeAccessToken.get();
            log.info("accessToken:{}", accessToken);

            //유효하지 않다면 401 응답, 프론트는 인증 서비스에 재발급 요청
            if (!jwtService.isValidToken(accessToken)) {
                log.info("accessToken 만료, 재발급 필요");
                throw new TokenExpiredException();
            }


            return blacklistClient.blacklist(new AccessTokenRequest(accessToken))
                    .flatMap(apiResponse -> {
                        // 블랙리스트에 토큰이 있다면 예외
                        if (apiResponse.getData()) {
                            log.info("블랙리스트에 속한 accessToken, 재발급 필요");
                            return Mono.error(new TokenExpiredException());
                        }

                        // 인증된 요청

                        log.info("인증된 사용자 요청");
                        return chain.filter(
                                setAuthenticationHeader(exchange, jwtService.extractUserInformation(accessToken)));
                    });

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

