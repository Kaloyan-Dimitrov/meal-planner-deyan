// ==== AuthController.java ====
package com.deyan.mealplanner.controller;

import com.deyan.mealplanner.dto.*;
import com.deyan.mealplanner.service.JwtUtil;
import com.deyan.mealplanner.service.RefreshTokenService;
import com.deyan.mealplanner.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtUtil jwt;
    private final UserService userService;
    private final RefreshTokenService rtService;

    /* ---------- LOGIN ---------- */
    @PostMapping("/login")
    public ResponseEntity<AuthTokenDTO> login(@RequestBody AuthRequest req) {

        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );

        UserDTO user = userService.findByEmail(req.email());

        String access  = jwt.generateToken(user, Duration.ofMinutes(15));
        String refresh = rtService.create(user.id(), req.rememberMe()); // add flag to AuthRequest

        return ResponseEntity.ok(new AuthTokenDTO(access, refresh));
    }

    /* ---------- REGISTER ---------- */
    @PostMapping("/register")
    public ResponseEntity<AuthTokenDTO> register(@RequestBody CreateUserRequest req) {

        UserDTO user = userService.createUser(req);

        String access  = jwt.generateToken(user, Duration.ofMinutes(15));
        String refresh = rtService.create(user.id(), false);

        return ResponseEntity.ok(new AuthTokenDTO(access, refresh));
    }

    /* ---------- REFRESH ---------- */
    @PostMapping("/refresh")
    public ResponseEntity<AuthTokenDTO> refresh(@RequestBody RefreshTokenDTO body) {
        Long userId = rtService.verifyAndGetUserId(body.refreshToken());

        UserDTO user = userService.getUserById(userId);          // already exists in your service
        String access = jwt.generateToken(user, Duration.ofMinutes(15));

        return ResponseEntity.ok(new AuthTokenDTO(access, null)); // no new refresh token
    }

    /* ---------- LOGOUT ---------- */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshTokenDTO body) {
        rtService.invalidate(body.refreshToken());
        return ResponseEntity.ok().build();
    }
}