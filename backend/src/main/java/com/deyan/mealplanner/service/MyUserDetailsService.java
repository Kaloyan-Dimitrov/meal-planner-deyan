package com.deyan.mealplanner.service;

import org.jooq.DSLContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.deyan.mealplanner.jooq.tables.Users.USERS;

@Service
public class MyUserDetailsService implements UserDetailsService {
    private final DSLContext dsl;

    /**
     * Constructs the service with a JOOQ DSL context.
     *
     * @param dsl The {@link DSLContext} used to query the database.
     */
    public MyUserDetailsService(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * Loads a user by email
     *
     * @param email The user's email address.
     * @return A Spring Security {@link UserDetails} object with roles and hashed password.
     * @throws UsernameNotFoundException If no user is found with the given email.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var user = dsl.selectFrom(USERS)
                .where(USERS.EMAIL.eq(email))
                .fetchOptional()
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities("USER")
                .build();
    }
}
