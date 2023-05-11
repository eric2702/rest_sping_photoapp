package com.soa_rest.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.soa_rest.services.AuthenticationService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/deauth")
@RequiredArgsConstructor
public class LogoutController {

    private final AuthenticationService authService;

    @PostMapping("/logout")
    // @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        return authService.logout(request);
    }

}
