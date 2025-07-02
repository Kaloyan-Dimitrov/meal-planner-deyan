package com.deyan.mealplanner.service

import com.deyan.mealplanner.AbstractIT          // <- your base class
import com.deyan.mealplanner.dto.CreateUserRequest
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Unroll
import static org.jooq.impl.DSL.*


import static com.deyan.mealplanner.jooq.tables.Users.USERS
import static com.deyan.mealplanner.jooq.tables.UserProgress.USER_PROGRESS

class UserServiceSpec extends AbstractIT{
    @Autowired
    UserService userService           // class under test

    @Autowired
    DSLContext dsl                    // handy for DB assertions

    def "createUser() persists a user and an initial weight entry"() {
        given:
        def req = new CreateUserRequest("Alice", "alice@mail.com", "secret", new BigDecimal("63.5"))

        when:
        def dto = userService.createUser(req)

        then: "DTO reflects what we sent"
        dto.name()      == "Alice"
        dto.email()     == "alice@mail.com"
        dto.weight()    == new BigDecimal("63.5")
        dto.dayStreak() == 0
        dto.id()        != null

        and: "row exists in USERS"
        boolean exists = dsl.fetchExists(selectOne().from(USERS).where(USERS.ID.eq(dto.id().intValue())))
        exists

        and: "row exists in USER_PROGRESS"
        dsl.fetchExists(
                dsl.selectOne()
                        .from(USER_PROGRESS)
                        .where(USER_PROGRESS.USER_ID.eq(dto.id().intValue()) & USER_PROGRESS.WEIGHT.eq(new BigDecimal("63.5")))
        )
    }

    def "createUser() throws if e-mail already exists"() {
        given: "an existing user"
        userService.createUser(new CreateUserRequest("Bob","bob@mail.com","pwd",new BigDecimal("70")))

        when:  userService.createUser(new CreateUserRequest("Bobby","bob@mail.com","pwd2",new BigDecimal("71")))
        then:  thrown(IllegalArgumentException)
    }

    @Unroll
    def "getUserById() returns latest weight entry (case: #variant)"() {
        given:
        def dto = userService.createUser(new CreateUserRequest("Cara","c+${variant}@x.com","pwd", startWeight as BigDecimal))
        and: "extra progress entries (if any)"
        extraWeights.each {
            userService.addUserWeightEntry(dto.id(), it as BigDecimal)
            sleep 5                                    // ensure date is later (spock tests execute fast!)
        }

        when:
        def result = userService.getUserById(dto.id())

        then:
        result.weight() == expectedWeight


        where:
        variant  | startWeight | extraWeights                         | expectedWeight
        "none"   | 60          | []                                   | 60
        "later"  | 65          | [66.3, 67.1]                         | 67.1
        "equal"  | 80          | [80]                                 | 80
    }

    def "deleteUserById() removes user and its progress"() {
        given:
        def dto = userService.createUser(new CreateUserRequest("Dan","dan@x.com","pwd", 50 as BigDecimal))

        when: "delete"
        userService.deleteUserById(dto.id())

        then:
        !dsl.fetchExists(dsl.selectOne().from(USERS).where(USERS.ID.eq(dto.id().intValue())))
        !dsl.fetchExists(dsl.selectOne().from(USER_PROGRESS).where(USER_PROGRESS.USER_ID.eq(dto.id().intValue())))
    }

    def "addUserWeightEntry() rejects unknown user id"() {
        when: userService.addUserWeightEntry(999L, new BigDecimal("77"))
        then: thrown(IllegalArgumentException)
    }
}
