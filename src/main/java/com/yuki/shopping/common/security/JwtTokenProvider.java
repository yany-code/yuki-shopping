package com.yuki.shopping.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

@Component
public class JwtTokenProvider {

    public enum TokenType { ACCESS, REFRESH }

    private final JwtProperties props;
    private final SecretKey key;

    public JwtTokenProvider(JwtProperties props) {
        this.props = props;
        this.key = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(LoginUser user) {
        return create(user, TokenType.ACCESS, Duration.ofMinutes(props.getAccessTokenTtlMinutes()));
    }

    public String createRefreshToken(LoginUser user) {
        return create(user, TokenType.REFRESH, Duration.ofDays(props.getRefreshTokenTtlDays()));
    }

    /** 解析并校验 token：签名、过期、签发者或类型不符均抛 JwtException */
    public LoginUser parse(String token, TokenType expectedType) {
        Claims claims = Jwts.parser().verifyWith(key).requireIssuer(props.getIssuer()).build()
                .parseSignedClaims(token).getPayload();
        if (!expectedType.name().equals(claims.get("tokenType", String.class))) {
            throw new JwtException("token 类型不匹配");
        }
        UserType userType = UserType.valueOf(claims.get("userType", String.class));
        return new LoginUser(Long.valueOf(claims.getSubject()), claims.get("username", String.class),
                userType, claims.get("role", Integer.class));
    }

    private String create(LoginUser user, TokenType type, Duration ttl) {
        Date now = new Date();
        var builder = Jwts.builder()
                .issuer(props.getIssuer())
                .subject(String.valueOf(user.userId()))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ttl.toMillis()))
                .claim("username", user.username())
                .claim("userType", user.userType().name())
                .claim("tokenType", type.name());
        if (user.role() != null) {
            builder.claim("role", user.role());
        }
        return builder.signWith(key).compact();
    }
}
