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

    public String create(Long userId, boolean rememberMe) {
        db.deleteFrom(REFRESH_TOKEN).where(REFRESH_TOKEN.USER_ID.eq(userId)).execute();
        Duration ttl  = rememberMe ? DEFAULT_TTL.multipliedBy(4) : DEFAULT_TTL;
        String   token = UUID.randomUUID().toString();

        LocalDateTime expiry = LocalDateTime.now(ZoneOffset.UTC).plusSeconds(ttl.getSeconds());

        db.insertInto(REFRESH_TOKEN)
                .set(REFRESH_TOKEN.USER_ID,  userId)
                .set(REFRESH_TOKEN.TOKEN,    token)
                .set(REFRESH_TOKEN.EXPIRY,   expiry)   // <-- now types match
                .execute();

        return token;
    }

    public Long verifyAndGetUserId(String token) {
        var record = db.selectFrom(REFRESH_TOKEN)
                .where(REFRESH_TOKEN.TOKEN.eq(token))
                .fetchOne();

        if (record == null) throw new RefreshTokenException("Invalid refresh token");
        if (record.getExpiry().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            db.deleteFrom(REFRESH_TOKEN).where(REFRESH_TOKEN.ID.eq(record.getId())).execute();
            throw new RefreshTokenException("Refresh token expired");
        }
        return record.getUserId();
    }

    public void invalidate(String token) {
        db.deleteFrom(REFRESH_TOKEN)
                .where(REFRESH_TOKEN.TOKEN.eq(token))
                .execute();
    }

}
