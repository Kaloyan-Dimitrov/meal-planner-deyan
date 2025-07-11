//package com.deyan.mealplanner.service
//
//import com.deyan.mealplanner.MealPlannerApplication
//import com.deyan.mealplanner.dto.CreateUserRequest
//import org.jooq.DSLContext
//import org.jooq.impl.DSL
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.context.ApplicationContext
//import org.springframework.security.crypto.password.PasswordEncoder
//import spock.lang.Specification
//import spock.lang.Stepwise
//@SpringBootTest(classes = MealPlannerApplication)
//@Stepwise
//class UserServiceSpec extends Specification  {
//
//    @Autowired(required = false) ApplicationContext ctx      //      A
//    @Autowired(required = false) UserService     userService
//    @Autowired DSLContext      dsl
//    @Autowired PasswordEncoder encoder
//
//    def setup() {
//        println "\n---- Spring ctx present? " + (ctx != null)
//        if (ctx) {
//            println "---- Beans of type UserService found: " +
//                    ctx.getBeanNamesForType(UserService).toList()
//        }
//    }
//    def "createUser() persists user and hashes password"() {
//        given:
//        def req = new CreateUserRequest("alice@mail.com","Alice","hunter2", 70 as BigDecimal)
//
//        when:
//        def dto = userService.createUser(req)
//
//        then:
//        dto != null
//        dsl.fetchCount(DSL.table("users"), DSL.field("id").eq(dto.id())) == 1
//        !encoder.matches("hunter2",
//                dsl.fetchValue("select password from users where id = ?", dto.id()) as String)
//    }
//
//
//    def "duplicate e-mail throws"() {
//        given: userService.createUser(
//                new CreateUserRequest("bob@mail.com","Bob","pw", 80G as BigDecimal))
//
//        when:  userService.createUser(
//                new CreateUserRequest("bob@mail.com","Bob","pw", 80G as BigDecimal))
//
//        then:
//        thrown(IllegalArgumentException)
//    }
//
//    def "deleteUserById() cleans up both tables"() {
//        given:
//        long id = userService.createUser(
//                new CreateUserRequest("eve@mail.com","Eve","pw", 60G as BigDecimal)).id()
//
//        when:
//        userService.deleteUserById(id)
//
//        then:
//        !dsl.fetchExists(
//                DSL.selectOne().from("users").where(DSL.field("id").eq(id)))
//        !dsl.fetchExists(
//                DSL.selectOne().from("user_progress").where(DSL.field("user_id").eq(id)))
//    }
//}
