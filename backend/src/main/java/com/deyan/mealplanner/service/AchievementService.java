package com.deyan.mealplanner.service;

import com.deyan.mealplanner.dto.AchievementDTO;
import com.deyan.mealplanner.exceptions.NotFoundException;
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

/**
 * Service responsible for managing user achievements.
 * <p>
 * Handles progress updates and retrieval of unlocked or in-progress achievements.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementService {

    private final DSLContext dsl;

    /**
     * Retrieves all achievements for a user, including their current progress and completion status.
     * Used for rendering the sidebar or achievement overview.
     *
     * @param userId The ID of the user.
     * @return A list of {@link AchievementDTO} objects with progress and unlock state.
     */
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
     * Updates the progress for a user's streak/log-related achievements after a new weight log.
     * Unlocks achievements when a target is reached and records the unlock time.
     *
     * @param userId    The ID of the user.
     * @param newStreak The current streak length.
     * @return A list of achievement IDs that were newly unlocked during this update.
     */
    public List<Long> updateAfterWeightLog(long userId, int newStreak) {
        int totalLogs = dsl.fetchCount(
                USER_PROGRESS,
                USER_PROGRESS.USER_ID.eq(userId)
        );

        Map<Long, Integer> map = Map.of(
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
        return unlocked;
    }

    /**
     * Updates or inserts a user's progress toward a specific achievement.
     * If the progress meets or exceeds the target and was previously locked, the achievement is marked as completed.
     *
     * @param userId   The ID of the user.
     * @param achId    The ID of the achievement.
     * @param progress The user's current progress toward the achievement.
     * @return True if the achievement was unlocked in this call; false otherwise.
     * @throws NotFoundException if the achievement does not exist.
     */
    private boolean saveProgress(long userId, long achId, int progress) {
        Integer target = dsl
                .select(ACHIEVEMENT.TARGET)
                .from(ACHIEVEMENT)
                .where(ACHIEVEMENT.ID.eq(achId))
                .fetchOneInto(Integer.class);

        if (target == null) {
            throw new NotFoundException("Achievement ID " + achId + " does not exist.");
        }

        var rec = dsl
                .select(USER_ACHIEVEMENT.PROGRESS, USER_ACHIEVEMENT.COMPLETED_AT)
                .from(USER_ACHIEVEMENT)
                .where(USER_ACHIEVEMENT.USER_ID.eq(userId)
                        .and(USER_ACHIEVEMENT.ACHIEVEMENT_ID.eq(achId)))
                .fetchOne();

        boolean wasLocked = rec == null || rec.get(USER_ACHIEVEMENT.COMPLETED_AT) == null;
        boolean unlockNow = wasLocked && progress >= target;

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

        return unlockNow;
    }
}
