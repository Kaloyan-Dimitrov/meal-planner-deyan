package com.deyan.mealplanner.controller;

import com.deyan.mealplanner.dto.*;
import com.deyan.mealplanner.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDTO> getAllUsers(){
        return userService.getAllUsers();
    }
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody CreateUserRequest req) {
        UserDTO created = userService.createUser(req);
        return ResponseEntity.status(201).body(created);
    }
    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable Long id){
        return userService.getUserById(id);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/weight")
    public WeightEntryDTO addWeightEntry(@PathVariable Long id, @RequestBody WeightEntryDTO weightEntryDTO){
        return userService.addUserWeightEntry(id,weightEntryDTO.weight());
    }
    @GetMapping("/{id}/weight")
    public List<WeightChartDTO> getUserWeightEntries(@PathVariable Long id){
     return userService.getRecentWeights(id);
    }
    @GetMapping("/{id}/achievements")
    public List<AchievementDTO> getAchievements(@PathVariable Long id){
        return userService.getUserAchievements(id);
    }
}
