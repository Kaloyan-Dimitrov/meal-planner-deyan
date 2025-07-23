package com.deyan.mealplanner.service;

import com.deyan.mealplanner.dto.UserDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    private final String SECRET;
    private final long EXPIRATION = 1000 * 60 * 60; // 1 hour

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.SECRET  = secret;
        System.out.println("JWT SECRET LOADED: " + secret);
    }
    public String generateToken(UserDTO user) {
        Map<String,Object> claims = new HashMap<>();
        claims.put("userId", user.id());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.email())             // sub = email
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    public Claims extractAllClaims(String token) {
        return parse(token).getBody();
    }

    /* ----------------------------------------------------------------
       3.  Robust validation –
           • signature & expiry (done by parse())
           • subject matches the user we just loaded
    ---------------------------------------------------------------- */
    public boolean isTokenValid(String token, UserDetails ud) {
        try {
            Claims c = extractAllClaims(token);   // signature & expiry
            return c.getSubject().equals(ud.getUsername());
        } catch (Exception e) {
            return false;
        }
    }
    public boolean isTokenValid(String token) {   // unused, kept for convenience
        try { extractAllClaims(token); return true; }
        catch (Exception e) { return false; }
    }

    /* ---------------------------------------------------------------- */
    private Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET.getBytes())
                .build()
                .parseClaimsJws(token);
    }
}
