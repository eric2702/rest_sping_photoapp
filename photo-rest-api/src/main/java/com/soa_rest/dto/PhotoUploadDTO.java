package com.soa_rest.dto;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class PhotoUploadDTO {
    // private String name;

    private String description;

    @NotNull(message = "File is required")
    private MultipartFile file;

    // public String getName() {
    // return name;
    // }

    // public void setName(String name) {
    // this.name = name;
    // }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

}
