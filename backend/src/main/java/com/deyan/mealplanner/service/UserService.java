package com.deyan.mealplanner.service;

import com.deyan.mealplanner.dto.*;
import com.deyan.mealplanner.exceptions.AlreadyExistsException;
import com.deyan.mealplanner.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.deyan.mealplanner.jooq.tables.UserProgress.USER_PROGRESS;
import static com.deyan.mealplanner.jooq.tables.Users.USERS;
import static org.jooq.impl.DSL.*;

/**
 * Service for managing user accounts, weight tracking, and achievements.
 */
@Service
@Slf4j
public class UserService {

    private final DSLContext dsl;
    private final PasswordEncoder passwordEncoder;
    private final AchievementService achievementService;

    /**
     * Constructs the service with required dependencies.
     *
     * @param dsl                 The JOOQ DSL context for database queries.
     * @param passwordEncoder     Encoder for securely storing passwords.
     * @param achievementService  Service for updating user achievements.
     */
    public UserService(DSLContext dsl, PasswordEncoder passwordEncoder, AchievementService achievementService) {
        this.dsl = dsl;
        this.passwordEncoder = passwordEncoder;
        this.achievementService = achievementService;
    }

    /**
     * Returns all users with their latest weight log (if any).
     */
    public List<UserDTO> getAllUsers() {
        // Subquery for latest progress
        Table<?> latestProgress = dsl
                .select(
                        USER_PROGRESS.USER_ID,
                        USER_PROGRESS.WEIGHT,
                        USER_PROGRESS.DATE,
                        DSL.rowNumber().over(
                                DSL.partitionBy(USER_PROGRESS.USER_ID)
                                        .orderBy(USER_PROGRESS.DATE.desc())
                        ).as("rn")
                )
                .from(USER_PROGRESS)
                .asTable("latest_progress");

        return dsl.select(
                        USERS.ID,
                        USERS.NAME,
                        USERS.EMAIL,
                        USERS.DAY_STREAK,
                        field(name("latest_progress", "weight"), BigDecimal.class),
                        field(name("latest_progress", "date"), LocalDateTime.class)
                )
                .from(USERS)
                .leftJoin(latestProgress)
                .on(field(name("latest_progress", "user_id"), Long.class).eq(USERS.ID.cast(Long.class))
                        .and(field(name("latest_progress", "rn"), Integer.class).eq(1)))
                .fetch()
                .map(record -> new UserDTO(
                        record.get(USERS.ID),
                        record.get(USERS.NAME),
                        record.get(USERS.EMAIL),
                        record.get(field(name("latest_progress", "weight"), BigDecimal.class)),
                        record.get(USERS.DAY_STREAK),
                        record.get(field(name("latest_progress", "date"), LocalDateTime.class))
                ));
    }

    /**
     * Finds a user by email.
     *
     * @param email Email address to search for.
     * @return UserDTO with latest weight log if exists.
     */
    public UserDTO findByEmail(String email) {
        var userRecord = dsl.selectFrom(USERS)
                .where(USERS.EMAIL.eq(email))
                .fetchOne();

        if (userRecord == null) {
            throw new NotFoundException("User not found with email: " + email);
        }

        var latestProgress = dsl.selectFrom(USER_PROGRESS)
                .where(USER_PROGRESS.USER_ID.eq(userRecord.getId()))
                .orderBy(USER_PROGRESS.DATE.desc())
                .limit(1)
                .fetchOne();

        return new UserDTO(
                userRecord.getId(),
                userRecord.getName(),
                userRecord.getEmail(),
                latestProgress != null ? latestProgress.getWeight() : null,
                userRecord.getDayStreak(),
                latestProgress != null ? latestProgress.getDate() : null
        );
    }

    /**
     * Creates a new user and logs initial weight.
     */
    public UserDTO createUser(CreateUserRequest request) {
        boolean emailExists = dsl.fetchExists(
                dsl.selectFrom(USERS).where(USERS.EMAIL.eq(request.email())));

        if (emailExists) {
            throw new AlreadyExistsException("Email already in use");
        }

        dsl.insertInto(USERS)
                .set(USERS.NAME, request.name())
                .set(USERS.EMAIL, request.email())
                .set(USERS.PASSWORD, passwordEncoder.encode(request.password()))
                .set(USERS.DAY_STREAK, 0)
                .execute();

        var userId = dsl.select(USERS.ID)
                .from(USERS)
                .where(USERS.EMAIL.eq(request.email()))
                .fetchOne(USERS.ID);

        var today = LocalDateTime.now();

        addUserWeightEntry(userId, request.weight());

        return new UserDTO(
                userId,
                request.name(),
                request.email(),
                request.weight(),
                1,
                today
        );
    }

    /**
     * Fetches a user by ID with latest weight entry.
     */
    public UserDTO getUserById(Long id) {
        var userRecord = dsl.selectFrom(USERS)
                .where(USERS.ID.eq(id))
                .fetchOne();

        if (userRecord == null) {
            throw new NotFoundException("User not found with ID: " + id);
        }

        var latestProgress = dsl.selectFrom(USER_PROGRESS)
                .where(USER_PROGRESS.USER_ID.eq(userRecord.getId()))
                .orderBy(USER_PROGRESS.DATE.desc())
                .limit(1)
                .fetchOne();

        return new UserDTO(
                userRecord.getId(),
                userRecord.getName(),
                userRecord.getEmail(),
                latestProgress != null ? latestProgress.getWeight() : null,
                userRecord.getDayStreak(),
                latestProgress != null ? latestProgress.getDate() : null
        );
    }

    /**
     * Deletes a user and all related progress entries.
     */
    public void deleteUserById(Long id) {
        dsl.deleteFrom(USER_PROGRESS)
                .where(USER_PROGRESS.USER_ID.eq(id))
                .execute();

        int deleted = dsl.deleteFrom(USERS)
                .where(USERS.ID.eq(id))
                .execute();

        if (deleted == 0) {
            throw new NotFoundException("User not found or already deleted");
        }
    }

    /**
     * Adds a new weight entry for a user and updates their streak.
     */
    public WeightEntryDTO addUserWeightEntry(Long userId, BigDecimal weight) {
        boolean exists = dsl.fetchExists(
                dsl.selectOne()
                        .from(USERS)
                        .where(USERS.ID.eq(userId)));

        if (!exists) {
            throw new NotFoundException("User not found with ID: " + userId);
        }

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        boolean alreadyLoggedToday = dsl.fetchExists(
                dsl.selectOne()
                        .from(USER_PROGRESS)
                        .where(USER_PROGRESS.USER_ID.eq(userId))
                        .and(USER_PROGRESS.DATE.cast(LocalDate.class).eq(today)));

        if (alreadyLoggedToday) {
            throw new AlreadyExistsException("You already logged your weight today.");
        }

        dsl.insertInto(USER_PROGRESS)
                .set(USER_PROGRESS.USER_ID, userId)
                .set(USER_PROGRESS.WEIGHT, weight)
                .set(USER_PROGRESS.DATE, now)
                .execute();

        int newStreak = 1;

        LocalDate lastEntryDate = dsl
                .select(USER_PROGRESS.DATE)
                .from(USER_PROGRESS)
                .where(USER_PROGRESS.USER_ID.eq(userId))
                .and(USER_PROGRESS.DATE.cast(LocalDate.class).lt(today))
                .orderBy(USER_PROGRESS.DATE.desc())
                .limit(1)
                .fetchOptionalInto(LocalDateTime.class)
                .map(LocalDateTime::toLocalDate)
                .orElse(null);

        Integer currentStreak = dsl
                .select(USERS.DAY_STREAK)
                .from(USERS)
                .where(USERS.ID.eq(userId))
                .fetchOneInto(Integer.class);

        if (currentStreak == null) currentStreak = 0;

        if (lastEntryDate != null && lastEntryDate.equals(today.minusDays(1))) {
            newStreak = currentStreak + 1;
        }

        dsl.update(USERS)
                .set(USERS.DAY_STREAK, newStreak)
                .where(USERS.ID.eq(userId))
                .execute();

        List<Long> unlocked = achievementService.updateAfterWeightLog(userId, newStreak);

        return new WeightEntryDTO(weight, now, unlocked);
    }

    /**
     * Returns a list of unlocked and in-progress achievements for the given user.
     */
    public List<AchievementDTO> getUserAchievements(Long userId) {
        return achievementService.getAchievementsForUser(userId);
    }

    /**
     * Returns the user's last 30 days of weight entries (for charting).
     */
    public List<WeightChartDTO> getRecentWeights(Long userId) {
        return dsl.select(USER_PROGRESS.DATE.cast(LocalDate.class), USER_PROGRESS.WEIGHT)
                .from(USER_PROGRESS)
                .where(USER_PROGRESS.USER_ID.eq(userId))
                .and(USER_PROGRESS.DATE.greaterOrEqual(LocalDateTime.now().minusDays(30)))
                .orderBy(USER_PROGRESS.DATE.asc())
                .fetchInto(WeightChartDTO.class);
    }
}
