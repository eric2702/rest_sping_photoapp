package com.soa_rest.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soa_rest.config.JwtService;
import com.soa_rest.helpers.response.ApiResponse;
import com.soa_rest.models.entities.Photo;
import com.soa_rest.models.entities.User;
import com.soa_rest.models.repos.UserRepo;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepo userRepo;

    public ResponseEntity<?> getUser(HttpServletRequest request) {
        // get user id from claims
        String token = request.getHeader("Authorization").substring(7);
        JwtService jwtService = new JwtService();
        Claims claims = jwtService.extractAllClaims(token);
        Integer userId = (Integer) claims.get("userId");
        String userUsername = (String) request.getAttribute("userUsername");
        // return the user id and username
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", userId);
        userMap.put("username", userUsername);

        String[] messages = { "User found" };
        ApiResponse response = new ApiResponse(true, messages, userMap);
        return ResponseEntity.ok(response);
    }

    // get user from user id
    public ResponseEntity<?> getUserById(Integer userId) {
        // get user id from claims
        Optional<User> user = userRepo.findById(userId);
        if (user.isEmpty()) {
            String[] message = { "User not found" };
            ApiResponse response = new ApiResponse(false, message);
            return ResponseEntity.badRequest().body(response);
        }
        String[] messages = { "User found" };
        // get user id and username and name from the user
        Map<String, Object> userMap = new HashMap<>();
        String userUsername = user.get().getUsername();
        String userName = user.get().getName();
        userMap.put("id", userId);
        userMap.put("username", userUsername);
        userMap.put("name", userName);
        ApiResponse response = new ApiResponse(true, messages, userMap);
        return ResponseEntity.ok(response);
    }

    // public ResponseEntity<?> save(User user) {
    // // check if username is already taken
    // if (userRepo.findByUsername(user.getUsername()) != null) {
    // String[] message = { "Username is already taken!" };
    // ApiResponse response = new ApiResponse(false, message);
    // return ResponseEntity.badRequest().body(response);
    // }
    // // bcrypt password before saving

    // User savedUser = userRepo.save(user);
    // String[] messages = { "User saved" };
    // ApiResponse response = new ApiResponse(true, messages, savedUser);
    // return ResponseEntity.status(HttpStatus.CREATED).body(response);
    // }

    // public ResponseEntity<?> login(User user) {
    // // check if username is already taken
    // User foundUser = userRepo.findByUsername(user.getUsername());
    // if (foundUser == null) {
    // String[] message = { "Username not found!" };
    // ApiResponse response = new ApiResponse(false, message);
    // return ResponseEntity.badRequest().body(response);
    // }
    // // check if password is correct
    // if (!foundUser.getPassword().equals(user.getPassword())) {
    // String[] message = { "Password is incorrect!" };
    // ApiResponse response = new ApiResponse(false, message);
    // return ResponseEntity.badRequest().body(response);
    // }
    // String[] messages = { "User logged in" };
    // ApiResponse response = new ApiResponse(true, messages, foundUser);
    // return ResponseEntity.ok(response);
    // }

}
