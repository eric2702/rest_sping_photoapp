package com.soa_rest.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.soa_rest.dto.CommentUploadDTO;
import com.soa_rest.helpers.response.ApiResponse;
import com.soa_rest.models.entities.Comment;
import com.soa_rest.models.entities.Photo;
import com.soa_rest.services.CommentService;
import com.soa_rest.services.PhotoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping
    public ResponseEntity create(@Valid @RequestBody CommentUploadDTO comment, BindingResult bindingResult,
            HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            // get all errors and store them in an array
            String[] errors = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage()).toArray(String[]::new);

            ApiResponse response = new ApiResponse(false, errors);
            return ResponseEntity.badRequest().body(response);
        }
        return commentService.save(comment, request);
    }

    @GetMapping("/all/{id}")
    public ResponseEntity getAllCommentsAndUsername(@PathVariable("id") Long id, HttpServletRequest request) {
        return commentService.getAllCommentsAndUsername(id, request);
    }

    @GetMapping("/size/{id}")
    public ResponseEntity getCommentsSize(@PathVariable("id") Long id) {
        return commentService.getCommentsSize(id);
    }

    @GetMapping("/some/{id}")
    public ResponseEntity getSomeCommentsAndUsername(@PathVariable("id") Long id, HttpServletRequest request) {
        return commentService.getSomeCommentsAndUsername(id, request);
    }

    // @PutMapping
    // public ResponseEntity update(@RequestBody Comment photo) {
    // return commentService.save(photo);
    // }

    @DeleteMapping("/{id}")
    public ResponseEntity removeOne(@PathVariable("id") Long id, HttpServletRequest request) {
        return commentService.removeOne(id, request);
    }

    @DeleteMapping("/all/{id}")
    public ResponseEntity removeAll(@PathVariable("id") Long id, HttpServletRequest request) {
        return commentService.removeAllByPhotoId(id, request);
    }
}
