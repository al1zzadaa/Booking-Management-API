package com.example.bookingmanagementapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.access-expiration-minutes}")
    private long accessExpirationMinutes;

    @Value("${jwt.refresh-expiration-days}")
    private long refreshExpirationDays;
    private final SecretKey key;

    public JwtServiceImpl() {
        String secret = "SADIG_SECRET_KEY_FOR_TEST_PROJECT_IN_MATRIX";
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }


    public String generateAccessToken(UserDetails userDetails) {

        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessExpirationMinutes * 60);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("type", "ACCESS")
                .signWith(key)
                .compact();
    }


    public String generateRefreshToken(UserDetails userDetails) {

        Instant now = Instant.now();
        Instant exp = now.plusSeconds(refreshExpirationDays * 24 * 60 * 60);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("type", "REFRESH")
                .signWith(key)
                .compact();
    }



//    public String generateToken(UserDetails userDetails) {
//
//        Instant now = Instant.now();
//        Instant exp = now.plusSeconds( expirationMinutes * 60);
//
//        return Jwts.builder()
//                .subject(userDetails.getUsername())
//                .issuedAt(Date.from(now))
//                .expiration(Date.from(exp)) // 1 час
//                .signWith(key)
//                .compact();
//    }

    public String extractEmail(String token) {
        return parseAllClaims(token).getSubject();
    }


    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractEmail(token);
        return username.equals(userDetails.getUsername()) && !isExpired(token);
    }

    public boolean isExpired(String token) {
        Date exp = parseAllClaims(token).getExpiration();
        return exp.before(new Date());
    }

    private Claims parseAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseClaimsJws(token)
                .getPayload();
    }
}
