package com.soa_rest.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.soa_rest.dto.UserLoginDTO;
import com.soa_rest.dto.UserRegisterDTO;
import com.soa_rest.helpers.response.ApiResponse;
import com.soa_rest.services.AuthenticationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authService;

    @PostMapping("/register")
    // @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegisterDTO request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            // create a Map to store the errors
            Map<String, String> errors = new HashMap();

            // iterate over each error and extract the field name and default message
            for (ObjectError error : bindingResult.getAllErrors()) {
                String fieldName = ((FieldError) error).getField();
                String defaultMessage = error.getDefaultMessage();

                // add the error to the errors Map using the field name as the key
                errors.put(fieldName, defaultMessage);
            }

            ApiResponse response = new ApiResponse(false, errors);
            return ResponseEntity.badRequest().body(response);
        }
        return authService.register(request);
    }

    @PostMapping("/authenticate")
    // @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<?> authenticate(@Valid @RequestBody UserLoginDTO request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            // get all errors and store them in an array
            String[] errors = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage()).toArray(String[]::new);

            ApiResponse response = new ApiResponse(false, errors);
            return ResponseEntity.badRequest().body(response);
        }
        return authService.authenticate(request);
    }

}
