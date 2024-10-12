package com.alphaka.gatewayservice.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    public static final String ID_CLAIM = "id";
    public static final String ROLE_CLAIM = "role";
    public static final String NICKNAME_CLAIM = "nickname";
    public static final String PROFILE_CLAIM = "profile";

    private static final String BEARER = "Bearer ";
    private static final String ACCESS_TOKEN_HEADER = "Authorization";

    @Value("${jwt.secretKey}")
    private String secretKey;

    private SecretKey key;

    @PostConstruct
    void initializeKey() {
        key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public Optional<String> extractAccessToken(ServerHttpRequest request) {
        return Optional.ofNullable(request.getHeaders().get(ACCESS_TOKEN_HEADER))
                .map(strings -> strings.get(0).replace(BEARER, ""));
    }

    public boolean isValidToken(String token) {
        try {
            Jws<Claims> claims = Jwts
                    .parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);

            return claims.getPayload().getExpiration().after(new Date());
        } catch (JwtException e) {
            return false;
        }
    }

    public Map<String, String> extractUserInformation(String token) {
        try {

            Claims payload = Jwts
                    .parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Map<String, String> userInformation = new HashMap<>();
            userInformation.put(ID_CLAIM, String.valueOf(payload.get(ID_CLAIM, Long.class)));
            userInformation.put(ROLE_CLAIM, payload.get(ROLE_CLAIM, String.class));
            userInformation.put(NICKNAME_CLAIM, payload.get(NICKNAME_CLAIM, String.class));
            userInformation.put(PROFILE_CLAIM, payload.get(PROFILE_CLAIM, String.class));

            return userInformation;
        } catch (JwtException e) {
            return null;
        }
    }


}
