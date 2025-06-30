package com.deyan.mealplanner.controller;

import com.deyan.mealplanner.dto.CreateUserRequest;
import com.deyan.mealplanner.dto.UserDTO;
import com.deyan.mealplanner.dto.WeightEntryDTO;
import com.deyan.mealplanner.service.UserService;
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
    public UserDTO createUser(@RequestBody CreateUserRequest userRequest){
        return userService.createUser(userRequest);
    }
    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable Long id){
        return userService.getUserById(id);
    }
    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable Long id){
        userService.deleteUserById(id);
    }
    @PostMapping("/{id}/weight")
    public WeightEntryDTO addWeightEntry(@PathVariable Long id, @RequestBody WeightEntryDTO weightEntryDTO){
        return userService.addUserWeightEntry(id,weightEntryDTO.weight());
    }
}
