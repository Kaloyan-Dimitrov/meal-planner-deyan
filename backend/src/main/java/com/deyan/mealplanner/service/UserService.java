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

@Service
@Slf4j
public class UserService {
    private final DSLContext dsl;
    private final PasswordEncoder passwordEncoder;
    private final AchievementService achievementService;

    public UserService(DSLContext dsl, PasswordEncoder passwordEncoder, AchievementService achievementService) {
        this.dsl = dsl;
        this.passwordEncoder = passwordEncoder;
        this.achievementService = achievementService;
    }

    public List<UserDTO> getAllUsers() {
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

// Step 2: Join users with their latest progress (where rn = 1)
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
    public UserDTO findByEmail(String email){
        var userRecord = dsl.selectFrom(USERS)
                .where(USERS.EMAIL.eq(email))
                .fetchOne();
        if(userRecord == null){
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
    public UserDTO createUser(CreateUserRequest request) {
        boolean emailExists = dsl.fetchExists(dsl.selectFrom(USERS).where(USERS.EMAIL.eq(request.email())));
                if(emailExists){
                    throw new AlreadyExistsException("Email already in use");
                }
        dsl.insertInto(USERS)
                .set(USERS.NAME, request.name())
                .set(USERS.EMAIL, request.email())
                .set(USERS.PASSWORD, passwordEncoder.encode(request.password()))
                .set(USERS.DAY_STREAK, 0)
                .execute(); // <— no returning

// then manually fetch the ID:
        var userId = dsl.select(USERS.ID)
                .from(USERS)
                .where(USERS.EMAIL.eq(request.email())) // or any unique field
                .fetchOne(USERS.ID);


        var today = LocalDateTime.now();

        dsl.insertInto(USER_PROGRESS)
                .set(USER_PROGRESS.USER_ID, userId)
                .set(USER_PROGRESS.WEIGHT, request.weight())
                .set(USER_PROGRESS.DATE, today)
                .execute();

        return new UserDTO(
                userId,
                request.name(),
                request.email(),
                request.weight(),
                0,
                today
        );
    }

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

    public void deleteUserById(Long id) {

        // Delete child records first
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
    public WeightEntryDTO addUserWeightEntry(Long userId, BigDecimal weight) {
        boolean exists = dsl.fetchExists(
                dsl.selectOne()
                        .from(USERS)
                        .where(USERS.ID.eq(userId))
        );

        if (!exists) {
            throw new NotFoundException("User not found with ID: " + userId);
        }

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // Check if user has already logged today
        boolean alreadyLoggedToday = dsl.fetchExists(
                dsl.selectOne()
                        .from(USER_PROGRESS)
                        .where(USER_PROGRESS.USER_ID.eq(userId))
                        .and(USER_PROGRESS.DATE.cast(LocalDate.class).eq(today))
        );
        if(alreadyLoggedToday){
            throw new AlreadyExistsException("You already logged your weight today.");
        }
        // Insert new weight entry
        dsl.insertInto(USER_PROGRESS)
                .set(USER_PROGRESS.USER_ID, userId)
                .set(USER_PROGRESS.WEIGHT, weight)
                .set(USER_PROGRESS.DATE, now)
                .execute();

        int newStreak = 1; // default to 1 if this is the first/only entry

        // Fetch latest entry date before today
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

        // Update streak in USERS table
        dsl.update(USERS)
                .set(USERS.DAY_STREAK, newStreak)
                .where(USERS.ID.eq(userId))
                .execute();
        List<Long> unlocked = achievementService.updateAfterWeightLog(userId, newStreak);

        return new WeightEntryDTO(weight, now,unlocked);
    }
    public List<AchievementDTO> getUserAchievements(Long userId) {
        return achievementService.getAchievementsForUser(userId);
    }

    public List<WeightChartDTO> getRecentWeights(Long userId) {
        return dsl.select(USER_PROGRESS.DATE.cast(LocalDate.class),USER_PROGRESS.WEIGHT)
                .from(USER_PROGRESS)
                .where(USER_PROGRESS.USER_ID.eq(userId))
                .and(USER_PROGRESS.DATE.greaterOrEqual(LocalDateTime.now().minusDays(30)))
                .orderBy(USER_PROGRESS.DATE.asc())
                .fetchInto(WeightChartDTO.class);
    }
}
