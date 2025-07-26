package com.deyan.mealplanner.service;

import com.deyan.mealplanner.dto.AchievementDTO;
import com.deyan.mealplanner.dto.CreateUserRequest;
import com.deyan.mealplanner.dto.UserDTO;
import com.deyan.mealplanner.dto.WeightEntryDTO;
import org.jooq.DSLContext;

import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static com.deyan.mealplanner.jooq.tables.Achievement.ACHIEVEMENT;
import static com.deyan.mealplanner.jooq.tables.UserAchievement.USER_ACHIEVEMENT;
import static com.deyan.mealplanner.jooq.tables.UserProgress.USER_PROGRESS;
import static com.deyan.mealplanner.jooq.tables.Users.USERS;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;

@Service
public class UserService {
    private final DSLContext dsl;
    private final PasswordEncoder passwordEncoder;

    public UserService(DSLContext dsl, PasswordEncoder passwordEncoder) {
        this.dsl = dsl;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserDTO> getAllUsers() {
//        Field<Integer> USER_ID = USER_PROGRESS.USER_ID;
//        Field<BigDecimal> WEIGHT = USER_PROGRESS.WEIGHT;
//        Field<LocalDate> DATE = USER_PROGRESS.DATE;
//
//// 2. Create the subquery and alias it properly
//        var lp = DSL
//                .select(USER_ID, WEIGHT, DATE)
//                .from(USER_PROGRESS)
//                .where(DSL.row(USER_ID, DATE).in(
//                        DSL.select(USER_ID, DSL.max(DATE))
//                                .from(USER_PROGRESS)
//                                .groupBy(USER_ID)
//                ))
//                .asTable("latest_progress");
//
//// 3. Extract fields from alias `lp`
//        Field<Integer> LP_USER_ID = lp.field(USER_ID);
//        Field<BigDecimal> LP_WEIGHT = lp.field(WEIGHT);
//        Field<LocalDate> LP_DATE = lp.field(DATE);
//
//// 4. Perform the join using the aliased fields
//        return dsl.select(
//                        USERS.ID,
//                        USERS.NAME,
//                        USERS.EMAIL,
//                        USERS.DAY_STREAK,
//                        LP_WEIGHT,
//                        LP_DATE
//                )
//                .from(USERS)
//                .leftJoin(lp)
//                .on(USERS.ID.eq(LP_USER_ID))
//                .fetch()
//                .map(record -> new UserDTO(
//                        record.get(USERS.ID).longValue(),
//                        record.get(USERS.NAME),
//                        record.get(USERS.EMAIL),
//                        record.get(LP_WEIGHT),
//                        record.get(USERS.DAY_STREAK),
//                        record.get(LP_DATE)
//                ));
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
                    throw new IllegalArgumentException("Email already in use");
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
        System.out.println("Inserting progress for user ID = " + userId);


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
            throw new RuntimeException("User not found");
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
            throw new RuntimeException("User not found or already deleted");
        }
    }
    public List<AchievementDTO> getUserAchievements(Long userId){
        return dsl.select(ACHIEVEMENT.ID, ACHIEVEMENT.NAME, ACHIEVEMENT.DESCRIPTION, USER_ACHIEVEMENT.COMPLETED_AT)
                .from(USER_ACHIEVEMENT)
                .join(ACHIEVEMENT).on(ACHIEVEMENT.ID.eq(USER_ACHIEVEMENT.ACHIEVEMENT_ID))
                .where(USER_ACHIEVEMENT.USER_ID.eq(userId))
                .fetchInto(AchievementDTO.class);
    }
    public WeightEntryDTO addUserWeightEntry(Long userId, BigDecimal weight) {
        boolean exists = dsl.fetchExists(
                dsl.selectOne()
                        .from(USERS)
                        .where(USERS.ID.eq(userId))
        );

        if (!exists) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
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
            throw new IllegalArgumentException("You already logged your weight today.");
        }
        // Insert new weight entry
        dsl.insertInto(USER_PROGRESS)
                .set(USER_PROGRESS.USER_ID, userId)
                .set(USER_PROGRESS.WEIGHT, weight)
                .set(USER_PROGRESS.DATE, now)
                .execute();

        int newStreak = 1; // default to 1 if this is the first/only entry

        if (!alreadyLoggedToday) {
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
        }
        checkAchievements(userId,newStreak);

        return new WeightEntryDTO(weight, now);
    }

    private void checkAchievements(Long userId, Integer newStreak) {
        Set<Long> completed = dsl.select(USER_ACHIEVEMENT.ACHIEVEMENT_ID)
                .from(USER_ACHIEVEMENT)
                .where(USER_ACHIEVEMENT.USER_ID.eq(userId))
                .fetchSet(USER_ACHIEVEMENT.ACHIEVEMENT_ID);

        // === Streak-based achievements ===
        if (newStreak >= 1 && !completed.contains(1L)) {
            assignAchievement(userId, 1L); // 1-day streak
        }
        if (newStreak >= 7 && !completed.contains(2L)) {
            assignAchievement(userId, 2L); // 7-day streak
        }
        if (newStreak >= 30 && !completed.contains(3L)) {
            assignAchievement(userId, 3L); // 30-day streak
        }

        // === Count-based achievements ===
        int totalLogs = dsl.fetchCount(USER_PROGRESS, USER_PROGRESS.USER_ID.eq(userId));

        if (totalLogs >= 10 && !completed.contains(4L)) {
            assignAchievement(userId, 4L); // 10 logs
        }
        if (totalLogs >= 20 && !completed.contains(5L)) {
            assignAchievement(userId, 5L); // 20 logs
        }
        if (totalLogs >= 30 && !completed.contains(6L)) {
            assignAchievement(userId, 6L); // 30 logs
        }
    }
    private void assignAchievement(Long userId, Long achievementId) {
        dsl.insertInto(USER_ACHIEVEMENT)
                .set(USER_ACHIEVEMENT.USER_ID, userId)
                .set(USER_ACHIEVEMENT.ACHIEVEMENT_ID, achievementId)
                .set(USER_ACHIEVEMENT.COMPLETED_AT, LocalDateTime.now())
                .execute();
    }
}
