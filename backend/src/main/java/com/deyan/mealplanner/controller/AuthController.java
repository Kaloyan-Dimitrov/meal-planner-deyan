// ==== AuthController.java ====
package com.deyan.mealplanner.controller;

import com.deyan.mealplanner.dto.AuthRequest;
import com.deyan.mealplanner.dto.CreateUserRequest;
import com.deyan.mealplanner.dto.UserDTO;
import com.deyan.mealplanner.service.JwtUtil;
import com.deyan.mealplanner.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthController(AuthenticationManager authManager,
                          JwtUtil jwtUtil,
                          UserService userService) {
        this.authManager   = authManager;
        this.jwtUtil       = jwtUtil;
        this.userService   = userService;

    }

    @PostMapping("/login")
    public ResponseEntity<Map<String,String>> login(@RequestBody AuthRequest req) {

        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );

        // service returns DTO (not entity)
        UserDTO user = userService.findByEmail(req.email());

        String token = jwtUtil.generateToken(user);   // now includes userId claim
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String,String>> register(@RequestBody CreateUserRequest req) {

        // persist + return DTO
        UserDTO user = userService.createUser(req);

        String token = jwtUtil.generateToken(user);   // auto-login
        return ResponseEntity.ok(Map.of("token", token));
    }
}