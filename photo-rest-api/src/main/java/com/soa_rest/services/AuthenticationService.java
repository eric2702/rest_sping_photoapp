package com.soa_rest.services;

import com.soa_rest.models.entities.User;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.soa_rest.config.JwtService;
import com.soa_rest.dto.UserLoginDTO;
import com.soa_rest.dto.UserRegisterDTO;
import com.soa_rest.helpers.response.ApiResponse;
import com.soa_rest.models.repos.UserRepo;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

        private final UserRepo userRepo;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final AuthenticationManager authenticationManager;

        public ResponseEntity<?> register(UserRegisterDTO request) {
                // get username and password from request
                String username = request.getUsername();
                String password = request.getPassword();
                // check if username is already taken
                Map<String, Object> responseBody = new HashMap<>();

                if (userRepo.findByUsername(username).isPresent()) {
                        String message = "Username is already taken";
                        // map message to username
                        responseBody.put("username", message);
                }
                // check if password minimum 8
                if (password.length() < 8) {
                        String message = "Password must be at least 8 characters";
                        responseBody.put("password", message);
                        // return AuthenticationReponse.builder().success(false)
                        // .message(message)
                        // .build();

                }
                if (!responseBody.isEmpty()) {
                        ApiResponse response = new ApiResponse(false, responseBody);
                        return ResponseEntity.status(400).body(response);
                }
                User user = User.builder()
                                .username(request.getUsername())
                                .name(request.getName())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .build();

                userRepo.save(user);
                var jwtToken = jwtService.generateToken(user, user.getId());
                // return AuthenticationReponse.builder().success(true)
                // .token(jwtToken)
                // .build();
                String[] message = { "User registered successfully" };
                Map<String, Object> respSuccess = new HashMap<>();
                respSuccess.put("token", jwtToken);
                ApiResponse response = new ApiResponse(true, message, respSuccess);
                return ResponseEntity.status(201).body(response);
        }

        public ResponseEntity<?> authenticate(UserLoginDTO request) {
                User user;
                try {
                        authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(request.getUsername(),
                                                        request.getPassword()));
                        user = userRepo.findByUsername(request.getUsername()).orElseThrow();
                } catch (Exception e) {
                        String[] message = { "Invalid username or password" };
                        ApiResponse response = new ApiResponse(false, message);
                        return ResponseEntity.status(400).body(response);
                }

                var jwtToken = jwtService.generateToken(user, user.getId());
                String[] message = { "User authenticated successfully" };
                // map jwt token to token
                Map<String, Object> responseBody = new HashMap<>();
                responseBody.put("token", jwtToken);

                ApiResponse response = new ApiResponse(true, message, responseBody);
                return ResponseEntity.ok(response);

        }

        public ResponseEntity<?> logout(HttpServletRequest request) {
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String jwt = authHeader.substring(7);
                        jwtService.expireToken(jwt);
                }
                String[] message = { "User logged out successfully" };
                ApiResponse response = new ApiResponse(true, message);
                return ResponseEntity.ok(response);
        }

}
