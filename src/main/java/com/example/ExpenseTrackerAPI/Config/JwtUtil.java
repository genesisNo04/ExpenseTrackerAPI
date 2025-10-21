package com.example.ExpenseTrackerAPI.Config;

import com.example.ExpenseTrackerAPI.Enum.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET_KEY = "";

    private static final long EXPIRATION_TIME = 1000 * 60 * 2;

    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    public String generateToken(String username, Role role) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .claim("role", role.name())
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims extractToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isTokenExpired(String token) {
        Claims claims = extractToken(token);
        if (claims == null) {
            return false;
        }
        return claims.getExpiration().before(new Date());
    }

    public boolean validateToken(String token, String username) {
        Claims claims = extractToken(token);
        if (claims == null) {
            return false;
        }

        return username.equals(claims.getSubject()) && !isTokenExpired(token);
    }

    public String extractUsername(String token) {
        Claims claims = extractToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    public Role extractRole(String token) {
        Claims claims = extractToken(token);
        return claims != null ? Role.valueOf(claims.get("role", String.class)) : null;
    }
}
