package com.deyan.mealplanner.service;

import com.deyan.mealplanner.dto.CreateUserRequest;
import com.deyan.mealplanner.dto.UserDTO;
import com.deyan.mealplanner.dto.WeightEntryDTO;
import org.jooq.DSLContext;

import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.util.List;

import static com.deyan.mealplanner.jooq.tables.UserProgress.USER_PROGRESS;
import static com.deyan.mealplanner.jooq.tables.Users.USERS;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;

@Service
public class UserService {
    private final DSLContext dsl;
    public UserService(DSLContext dsl) {
        this.dsl = dsl;
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

    public UserDTO createUser(CreateUserRequest request) {
        boolean emailExists = dsl.fetchExists(dsl.selectFrom(USERS).where(USERS.EMAIL.eq(request.email())));
                if(emailExists){
                    throw new IllegalArgumentException("Email already in use");
                }
        dsl.insertInto(USERS)
                .set(USERS.NAME, request.name())
                .set(USERS.EMAIL, request.email())
                .set(USERS.PASSWORD, request.password())
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

    public WeightEntryDTO addUserWeightEntry(Long userId, BigDecimal weight) {
        boolean exists = dsl.fetchExists(
                dsl.selectOne()
                        .from(USERS)
                        .where(USERS.ID.eq(userId))
        );

        if (!exists) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }

        var today = LocalDateTime.now();

        dsl.insertInto(USER_PROGRESS)
                .set(USER_PROGRESS.USER_ID, userId)
                .set(USER_PROGRESS.WEIGHT, weight)
                .set(USER_PROGRESS.DATE, today)
                .execute();

        return new WeightEntryDTO(weight, today);
    }
}
