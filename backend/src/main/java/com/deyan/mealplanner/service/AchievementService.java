package com.deyan.mealplanner.service.interfaces;
import com.deyan.mealplanner.dto.AchievementDTO;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

import static com.deyan.mealplanner.jooq.tables.Achievement.ACHIEVEMENT;
import static com.deyan.mealplanner.jooq.tables.UserAchievement.USER_ACHIEVEMENT;
import static com.deyan.mealplanner.jooq.tables.UserProgress.USER_PROGRESS;
import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.selectOne;


@Service
@RequiredArgsConstructor
public class AchievementService {
    private final DSLContext dsl;

    /* ───────────────────── public API ───────────────────── */

    /** Return all achievements for sidebar. */
    public List<AchievementDTO> getForUser(long userId) {
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
            if (saveProgress(userId, e.getKey(), e.getValue()))
                unlocked.add(e.getKey());
        }
        return unlocked;
    }

    /* ───────────────── private helpers ──────────────────── */

    private boolean saveProgress(long userId, long achId, int progress) {
        int target = dsl.select(ACHIEVEMENT.TARGET)
                .from(ACHIEVEMENT)
                .where(ACHIEVEMENT.ID.eq(achId))
                .fetchOneInto(Integer.class);

        var record = dsl.select(USER_ACHIEVEMENT.PROGRESS, USER_ACHIEVEMENT.COMPLETED_AT)
                .from(USER_ACHIEVEMENT)
                .where(USER_ACHIEVEMENT.USER_ID.eq(userId)
                        .and(USER_ACHIEVEMENT.ACHIEVEMENT_ID.eq(achId)))
                .fetchOne();

        boolean wasUnlocked = record != null && record.get(USER_ACHIEVEMENT.COMPLETED_AT) != null;
        boolean shouldUnlock = progress >= target;

        if (record == null) {
            dsl.insertInto(USER_ACHIEVEMENT)
                    .set(USER_ACHIEVEMENT.USER_ID, userId)
                    .set(USER_ACHIEVEMENT.ACHIEVEMENT_ID, achId)
                    .set(USER_ACHIEVEMENT.PROGRESS, progress)
                    .set(USER_ACHIEVEMENT.COMPLETED_AT, shouldUnlock ? LocalDateTime.now() : null)
                    .execute();
        } else {
            dsl.update(USER_ACHIEVEMENT)
                    .set(USER_ACHIEVEMENT.PROGRESS, progress)
                    .set(USER_ACHIEVEMENT.COMPLETED_AT,
                            shouldUnlock ? LocalDateTime.now() : record.get(USER_ACHIEVEMENT.COMPLETED_AT))
                    .where(USER_ACHIEVEMENT.USER_ID.eq(userId)
                            .and(USER_ACHIEVEMENT.ACHIEVEMENT_ID.eq(achId)))
                    .execute();
        }
        return !wasUnlocked && shouldUnlock;
    }
}
