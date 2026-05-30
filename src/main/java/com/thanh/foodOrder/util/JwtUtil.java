package com.thanh.foodOrder.util;

import java.security.Key;
import java.util.Date;
import java.util.Optional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.thanh.foodOrder.dtos.response.auth.ResponseLoginDTO;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;

@Component
public class JwtUtil {
    public String SECRET_KEY = "769e1dd6cb50f9a08e0794db8292ae8e9fd126280c5d017ceb62e314d89a3f547c208704f643fe196e951987a2cf1b41";
    private long JWT_EXPIRATION = 1 * 60 * 60 * 1000; // 1hours
    private long REFRESHTOKEN_EXPIRATION = 2 * 24 * 60 * 60 * 1000; // 2days
    // private long JWT_EXPIRATION = 10000; // 10s
    // private long REFRESHTOKEN_EXPIRATION = 10000;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String generateToken(String username, ResponseLoginDTO res) {
        ResponseLoginDTO.UserInsideToken userInsideToken = new ResponseLoginDTO.UserInsideToken();
        userInsideToken.setEmail(res.getUserLogin().getEmail());
        userInsideToken.setFullName(res.getUserLogin().getFullname());
        userInsideToken.setId(res.getUserLogin().getId());

        return Jwts.builder()
                .setSubject(username)
                .claim("user", userInsideToken)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION)) // now + expiration
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

    }

    public String generateRefreshToken(String username, ResponseLoginDTO res) {
        ResponseLoginDTO.UserInsideToken userInsideToken = new ResponseLoginDTO.UserInsideToken();
        userInsideToken.setEmail(res.getUserLogin().getEmail());
        userInsideToken.setFullName(res.getUserLogin().getFullname());
        userInsideToken.setId(res.getUserLogin().getId());
        return Jwts.builder()
                .setSubject(username)
                .claim("user", userInsideToken)

                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESHTOKEN_EXPIRATION))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

    }

    public boolean validateToken(String token, String username) {
        String extractedUsername = extractUsername(token);
        return extractedUsername.equals(username) && !isTokenExpired(token);
    }

    public boolean validRefreshToken(String token) {
        return !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date()); // true=>expired
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static Optional<String> getCurrentUserLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        return Optional.ofNullable(authentication.getName());
    }

}
