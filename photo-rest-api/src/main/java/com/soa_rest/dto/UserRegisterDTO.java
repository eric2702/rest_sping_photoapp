package com.soa_rest.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;

public class UserRegisterDTO {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Username is required")
    @Length(min = 5, max = 20, message = "Username must be between 5 and 20 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Length(min = 8, message = "Password must be at least 8 characters")
    private String password;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
