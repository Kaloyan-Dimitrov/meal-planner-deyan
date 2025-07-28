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

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    private final String SECRET;
    private static final Duration DEFAULT_ACCESS_TTL = Duration.ofMinutes(15);

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.SECRET  = secret;
        System.out.println("JWT SECRET LOADED: " + secret);
    }
    public String generateToken(UserDTO user, Duration ttl) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.id());

        Date now = new Date();
        Date exp = new Date(now.getTime() + ttl.toMillis());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.email())          // sub = email
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()), SignatureAlgorithm.HS256)
                .compact();

    }
    public String generateToken(UserDTO user) {
        return generateToken(user, DEFAULT_ACCESS_TTL);
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
