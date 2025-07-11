//package com.deyan.mealplanner;
//
//
//import com.deyan.mealplanner.dto.CreateUserRequest;
//import com.deyan.mealplanner.service.UserService;
//import org.jooq.DSLContext;
//import org.jooq.impl.DSL;
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.math.BigDecimal;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest   // boots the whole Spring context
//public class UserServiceSpec extends AbstractIT {
//    @Autowired
//    UserService userService;
//    @Autowired DSLContext      dsl;
//    @Autowired PasswordEncoder encoder;
//
//    @Test
//    void createUser_persists_and_hashes_password() {
//        // arrange
//        var req = new CreateUserRequest(
//                "alice@mail.com", "Alice", "hunter2", new BigDecimal("70"));
//
//        // act
//        var dto = userService.createUser(req);
//
//        // assert: row exists
//        long rows = dsl.fetchCount(
//                DSL.table("users"),
//                DSL.field("id").eq(dto.id()));
//        assertEquals(1, rows);
//
//        // assert: stored password is not plaintext
//        String hashed = dsl.fetchOne("select password from users where id = ?", dto.id())
//                .get(0, String.class);
//        assertFalse(encoder.matches("hunter2", hashed));
//    }
//
//    @Test
//    void duplicate_email_throws() {
//        var req = new CreateUserRequest(
//                "bob@mail.com", "Bob", "pw", new BigDecimal("80"));
//        userService.createUser(req);
//
//        assertThrows(IllegalArgumentException.class, () ->
//                userService.createUser(req));
//    }
//
//    @Test
//    void deleteUserById_removes_from_all_tables() {
//        var id = userService.createUser(
//                new CreateUserRequest("eve@mail.com", "Eve", "pw", new BigDecimal("60"))
//        ).id();
//
//        userService.deleteUserById(id);
//
//        boolean userExists = dsl.fetchExists(
//                DSL.selectOne().from("users").where(DSL.field("id").eq(id)));
//        boolean progressExists = dsl.fetchExists(
//                DSL.selectOne().from("user_progress").where(DSL.field("user_id").eq(id)));
//
//        assertFalse(userExists);
//        assertFalse(progressExists);
//    }
//}
