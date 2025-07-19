// ==== AuthController.java ====
package com.deyan.mealplanner.controller;

import com.deyan.mealplanner.dto.AuthRequest;
import com.deyan.mealplanner.dto.CreateUserRequest;
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
    private final PasswordEncoder encoder;

    public AuthController(AuthenticationManager authManager,
                          JwtUtil jwtUtil,
                          UserService userService,
                          PasswordEncoder encoder) {
        this.authManager   = authManager;
        this.jwtUtil       = jwtUtil;
        this.userService   = userService;
        this.encoder       = encoder;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String,String>> login(@RequestBody AuthRequest request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        return ResponseEntity.ok(Map.of("token", jwtUtil.generateToken(request.email())));
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody CreateUserRequest req) {

        // 1. persist user
        userService.createUser(req);

        // 3. auto-login
        String token = jwtUtil.generateToken(req.email());
        return ResponseEntity.ok(token);
    }
}