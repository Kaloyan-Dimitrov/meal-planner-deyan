package com.deyan.mealplanner.service;

import com.deyan.mealplanner.dto.UserDTO;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static com.deyan.mealplanner.jooq.tables.UserProgress.USER_PROGRESS;
import static com.deyan.mealplanner.jooq.tables.Users.USERS;

@Service
public class UserService {
    private final DSLContext dsl;
    public UserService(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<UserDTO> getAllUsers() {
        var latestProgress = dsl
                .select(USER_PROGRESS.USER_ID, DSL.max(USER_PROGRESS.DATE).as("latest_date"))
                .from(USER_PROGRESS)
                .groupBy(USER_PROGRESS.USER_ID)
                .asTable("latest");

        return dsl.select(
                        USERS.ID,
                        USERS.NAME,
                        USERS.EMAIL,
                        USERS.DAY_STREAK,
                        USER_PROGRESS.WEIGHT,
                        USER_PROGRESS.DATE
                )
                .from(USERS)
                .leftJoin(USER_PROGRESS)
                .on(USER_PROGRESS.USER_ID.eq(USERS.ID))
                .leftJoin(latestProgress)
                .on(USER_PROGRESS.USER_ID.eq(latestProgress.field("user_id", Integer.class))
                        .and(USER_PROGRESS.DATE.eq(latestProgress.field("latest_date", LocalDate.class))))
                .fetch()
                .map(record -> new UserDTO(
                        record.get(USERS.ID).longValue(),
                        record.get(USERS.NAME),
                        record.get(USERS.EMAIL),
                        record.get(USER_PROGRESS.WEIGHT),
                        record.get(USERS.DAY_STREAK),
                        record.get(USER_PROGRESS.DATE)
                ));
    }
}
