package com.deyan.mealplanner.controller;

import com.deyan.mealplanner.dto.*;
import com.deyan.mealplanner.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing users, weight tracking, and user achievements.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    /**
     * Constructs the controller with the required {@link UserService}.
     *
     * @param userService The service handling user-related operations.
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Retrieves a list of all users.
     *
     * @return A list of {@link UserDTO} objects.
     */
    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    /**
     * Creates a new user from the provided request data.
     *
     * @param req The request body containing user information.
     * @return The created {@link UserDTO}, wrapped in a 201 Created response.
     */
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody CreateUserRequest req) {
        UserDTO created = userService.createUser(req);
        return ResponseEntity.status(201).body(created);
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param id The ID of the user to retrieve.
     * @return The corresponding {@link UserDTO}.
     */
    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    /**
     * Deletes a user by their ID.
     *
     * @param id The ID of the user to delete.
     * @return HTTP 204 No Content on success.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Adds a weight entry for the specified user.
     *
     * @param id The ID of the user.
     * @param weightEntryDTO The request body containing the weight value.
     * @return A {@link WeightEntryDTO} with the recorded weight data.
     */
    @PostMapping("/{id}/weight")
    public WeightEntryDTO addWeightEntry(@PathVariable Long id, @RequestBody WeightEntryDTO weightEntryDTO) {
        return userService.addUserWeightEntry(id, weightEntryDTO.weight());
    }

    /**
     * Retrieves the most recent 30 weight entries for the specified user.
     *
     * @param id The ID of the user.
     * @return A list of {@link WeightChartDTO} entries for graphing weight over time.
     */
    @GetMapping("/{id}/weight")
    public List<WeightChartDTO> getUserWeightEntries(@PathVariable Long id) {
        return userService.getRecentWeights(id);
    }

    /**
     * Retrieves the list of achievements unlocked by the user.
     *
     * @param id The ID of the user.
     * @return A list of {@link AchievementDTO} objects.
     */
    @GetMapping("/{id}/achievements")
    public List<AchievementDTO> getAchievements(@PathVariable Long id) {
        return userService.getUserAchievements(id);
    }
}
