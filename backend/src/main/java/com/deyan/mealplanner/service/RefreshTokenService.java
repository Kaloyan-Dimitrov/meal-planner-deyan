package com.deyan.mealplanner.service;

import com.deyan.mealplanner.exceptions.RefreshTokenException;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static com.deyan.mealplanner.jooq.tables.RefreshToken.REFRESH_TOKEN;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final DSLContext db;

    private static final Duration DEFAULT_TTL = Duration.ofDays(7);

    /**
     * Creates a new refresh token for the given user, optionally with a longer TTL if "remember me" is selected.
     * Replaces any existing token for the user.
     *
     * @param userId     ID of the user to associate with the token.
     * @param rememberMe If true, extends the token TTL (e.g., 28 days).
     * @return A new random refresh token string.
     */
    public String create(Long userId, boolean rememberMe) {
        db.deleteFrom(REFRESH_TOKEN)
                .where(REFRESH_TOKEN.USER_ID.eq(userId))
                .execute();

        Duration ttl = rememberMe ? DEFAULT_TTL.multipliedBy(4) : DEFAULT_TTL;
        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now(ZoneOffset.UTC).plusSeconds(ttl.getSeconds());

        db.insertInto(REFRESH_TOKEN)
                .set(REFRESH_TOKEN.USER_ID, userId)
                .set(REFRESH_TOKEN.TOKEN, token)
                .set(REFRESH_TOKEN.EXPIRY, expiry)
                .execute();

        return token;
    }

    /**
     * Verifies the given refresh token and returns the associated user ID if valid.
     * Automatically deletes expired tokens.
     *
     * @param token The refresh token to verify.
     * @return The ID of the user associated with the token.
     * @throws RefreshTokenException if the token is invalid or expired.
     */
    public Long verifyAndGetUserId(String token) {
        var record = db.selectFrom(REFRESH_TOKEN)
                .where(REFRESH_TOKEN.TOKEN.eq(token))
                .fetchOne();

        if (record == null) {
            throw new RefreshTokenException("Invalid refresh token");
        }

        if (record.getExpiry().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            db.deleteFrom(REFRESH_TOKEN)
                    .where(REFRESH_TOKEN.ID.eq(record.getId()))
                    .execute();
            throw new RefreshTokenException("Refresh token expired");
        }

        return record.getUserId();
    }

    /**
     * Invalidates a refresh token by removing it from the database.
     *
     * @param token The token to invalidate.
     */
    public void invalidate(String token) {
        db.deleteFrom(REFRESH_TOKEN)
                .where(REFRESH_TOKEN.TOKEN.eq(token))
                .execute();
    }

}
