package com.soa_rest.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.soa_rest.dto.UserRegisterDTO;
import com.soa_rest.helpers.response.ApiResponse;
import com.soa_rest.models.entities.Photo;
import com.soa_rest.models.entities.User;
import com.soa_rest.services.PhotoService;
import com.soa_rest.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    // @PostMapping("/register")
    // public ResponseEntity create(@Valid @RequestBody UserRegisterDTO
    // userRegisDTO, BindingResult bindingResult) {

    // if (bindingResult.hasErrors()) {
    // // get all errors and store them in an array
    // String[] errors = bindingResult.getAllErrors().stream()
    // .map(error -> error.getDefaultMessage()).toArray(String[]::new);

    // ApiResponse response = new ApiResponse(false, errors);
    // return ResponseEntity.badRequest().body(response);
    // }

    // User user = new User();
    // user.setName(userRegisDTO.getName());
    // user.setUsername(userRegisDTO.getUsername());
    // user.setPassword(userRegisDTO.getPassword());

    // return userService.save(user);
    // }

    @GetMapping("/details")
    // @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity getDetails(HttpServletRequest request) {
        return userService.getUser(request);
    }

    // get user by id
    @GetMapping("/{id}")
    public ResponseEntity getUserById(@PathVariable("id") Integer id) {
        return userService.getUserById(id);
    }

}
