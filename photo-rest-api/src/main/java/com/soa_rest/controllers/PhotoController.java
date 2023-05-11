package com.soa_rest.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.soa_rest.dto.PhotoUploadDTO;
import com.soa_rest.helpers.response.ApiResponse;
import com.soa_rest.models.entities.Photo;
import com.soa_rest.services.PhotoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/photos")
public class PhotoController {

    @Autowired
    private PhotoService photoService;

    @PostMapping("/upload")
    public ResponseEntity uploadFile(@Valid @ModelAttribute PhotoUploadDTO request, HttpServletRequest servletRequest,
            BindingResult bindingResult)
            throws IllegalStateException, IOException {
        if (bindingResult.hasErrors()) {
            // get all errors and store them in an array
            String[] errors = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage()).toArray(String[]::new);

            ApiResponse response = new ApiResponse(false, errors);
            return ResponseEntity.badRequest().body(response);
        }
        return photoService.uploadFile(request, servletRequest);
    }

    @GetMapping("/all")
    ResponseEntity getAllPhotosAndSomeComments(HttpServletRequest request) {
        return photoService.getAllPhotosAndSomeComments(request);
    }

    @GetMapping("/{id}")
    public ResponseEntity getOnePhotoAndAllComments(@PathVariable("id") Long id, HttpServletRequest request) {
        return photoService.getOnePhotoAndAllComments(id, request);
    }

    @GetMapping("/details/{id}")
    public ResponseEntity findOne(@PathVariable("id") Long id) {
        return photoService.findOne(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity removeOne(@PathVariable("id") Long id, HttpServletRequest request) {
        return photoService.removeOne(id, request);
    }
}
