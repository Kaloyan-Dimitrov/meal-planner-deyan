package com.deyan.mealplanner.service;
import com.deyan.mealplanner.dto.AchievementDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

import static com.deyan.mealplanner.jooq.tables.Achievement.ACHIEVEMENT;
import static com.deyan.mealplanner.jooq.tables.UserAchievement.USER_ACHIEVEMENT;
import static com.deyan.mealplanner.jooq.tables.UserProgress.USER_PROGRESS;
import static org.jooq.impl.DSL.coalesce;


@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementService {
    private final DSLContext dsl;

    /* ───────────────────── public API ───────────────────── */

    /** Return all achievements for sidebar. */
    public List<AchievementDTO> getAchievementsForUser(long userId) {
        return dsl
                .select(
                        ACHIEVEMENT.ID,
                        ACHIEVEMENT.NAME,
                        ACHIEVEMENT.DESCRIPTION,
                        ACHIEVEMENT.TARGET,
                        coalesce(USER_ACHIEVEMENT.PROGRESS, 0).as("progress"),
                        USER_ACHIEVEMENT.COMPLETED_AT
                )
                .from(ACHIEVEMENT)
                .leftJoin(USER_ACHIEVEMENT)
                .on(USER_ACHIEVEMENT.ACHIEVEMENT_ID.eq(ACHIEVEMENT.ID)
                        .and(USER_ACHIEVEMENT.USER_ID.eq(userId)))
                .fetchInto(AchievementDTO.class);
    }

    /**
     * Update streak/log achievements after a new weight entry.
     * @return list of IDs that were unlocked in this call
     */
    public List<Long> updateAfterWeightLog(long userId, int newStreak) {

        int totalLogs = dsl.fetchCount(
                USER_PROGRESS,
                USER_PROGRESS.USER_ID.eq(userId)
        );

        Map<Long,Integer> map = Map.of(
                1L, newStreak,   // 1-day streak
                2L, newStreak,   // 7-day streak
                3L, newStreak,   // 30-day streak
                4L, totalLogs,   // 10 logs
                5L, totalLogs,   // 20 logs
                6L, totalLogs    // 30 logs
        );

        List<Long> unlocked = new ArrayList<>();
        for (var e : map.entrySet()) {
            boolean isUnlocked = saveProgress(userId, e.getKey(), e.getValue());
            if (isUnlocked) {
                unlocked.add(e.getKey());
            }
        }
        log.debug("Unlocked this call ➜ {}", unlocked);
        return unlocked;
    }

    /* ───────────────── private helpers ──────────────────── */

    private boolean saveProgress(long userId, long achId, int progress) {
        log.info("Progress is {}", progress);

        int target = dsl
                .select(ACHIEVEMENT.TARGET)
                .from(ACHIEVEMENT)
                .where(ACHIEVEMENT.ID.eq(achId))
                .fetchOneInto(Integer.class);

        // 2️⃣ Fetch the row *before* we change anything
        var rec = dsl
                .select(USER_ACHIEVEMENT.PROGRESS,
                        USER_ACHIEVEMENT.COMPLETED_AT)
                .from(USER_ACHIEVEMENT)
                .where(USER_ACHIEVEMENT.USER_ID.eq(userId)
                        .and(USER_ACHIEVEMENT.ACHIEVEMENT_ID.eq(achId)))
                .fetchOne();

        int prevProgress   = rec == null ? 0 : rec.get(USER_ACHIEVEMENT.PROGRESS);
        boolean wasLocked  = rec == null || rec.get(USER_ACHIEVEMENT.COMPLETED_AT) == null;
        boolean unlockNow  = wasLocked && progress >= target;   // ← our trigger

        // 3️⃣ Upsert progress
        if (rec == null) {
            dsl.insertInto(USER_ACHIEVEMENT)
                    .set(USER_ACHIEVEMENT.USER_ID, userId)
                    .set(USER_ACHIEVEMENT.ACHIEVEMENT_ID, achId)
                    .set(USER_ACHIEVEMENT.PROGRESS, progress)
                    .set(USER_ACHIEVEMENT.COMPLETED_AT, unlockNow ? LocalDateTime.now() : null)
                    .execute();
        } else {
            dsl.update(USER_ACHIEVEMENT)
                    .set(USER_ACHIEVEMENT.PROGRESS, progress)
                    .set(USER_ACHIEVEMENT.COMPLETED_AT,
                            unlockNow ? LocalDateTime.now() : rec.get(USER_ACHIEVEMENT.COMPLETED_AT))
                    .where(USER_ACHIEVEMENT.USER_ID.eq(userId)
                            .and(USER_ACHIEVEMENT.ACHIEVEMENT_ID.eq(achId)))
                    .execute();
        }
        log.debug("DBG achId={}  prevLocked={}  progress={}  target={}  unlockNow={}",
                achId, wasLocked, progress, target, unlockNow);
        return unlockNow;         // true only on the first completion
    }
}
