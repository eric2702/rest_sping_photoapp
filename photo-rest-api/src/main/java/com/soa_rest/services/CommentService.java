package com.soa_rest.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.soa_rest.config.JwtService;
import com.soa_rest.dto.CommentUploadDTO;
import com.soa_rest.helpers.response.ApiResponse;
import com.soa_rest.models.entities.Comment;
import com.soa_rest.models.entities.Photo;
import com.soa_rest.models.repos.CommentRepo;
import com.soa_rest.models.repos.PhotoRepo;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

@Service
@Transactional
public class CommentService {

    @Autowired
    private CommentRepo commentRepo;
    @Autowired
    private PhotoRepo photoRepo;
    @Autowired
    private RestTemplate restTemplate;

    public ResponseEntity<?> getCommentsSize(Long photoId) {
        List<Comment> comments = commentRepo.findByPhotoId(photoId);
        Map<String, Integer> map = new HashMap<>();
        map.put("commentsSize", comments.size());
        String[] message = { "Successfully got comments size" };
        ApiResponse response = new ApiResponse(true, message, map);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> save(CommentUploadDTO request, HttpServletRequest servletRequest) {
        // get user id from claims
        String token = servletRequest.getHeader("Authorization").substring(7);
        JwtService jwtService = new JwtService();
        Claims claims = jwtService.extractAllClaims(token);
        Integer userId = (Integer) claims.get("userId");

        // get photo id from comment using getPhotoId()
        Long photoId = request.getPhotoId();
        // find photo by id
        Optional<Photo> photoOptional = photoRepo.findById(photoId);
        // if photo not found, return with 404 status
        if (!photoOptional.isPresent()) {
            String[] message = { "Photo not found with id: " + photoId };
            ApiResponse response = new ApiResponse(false, message);
            // return with above message and 404 status
            return ResponseEntity.status(404).body(response);
        }
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setComment(request.getComment());
        comment.setPhotoId(photoId);
        Comment savedComment = commentRepo.save(comment);
        String[] message = { "Comment saved" };
        ApiResponse response = new ApiResponse(true, message, savedComment);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    public ResponseEntity<?> getSomeCommentsAndUsername(Long photoId, HttpServletRequest servletRequest) {

        String accessToken = servletRequest.getHeader("Authorization").substring(7);

        String photoUrl = "http://localhost:8080/api/photos/details/" + photoId;

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);

        // create a new HttpEntity object with the headers

        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
        ResponseEntity<JsonNode> photoResponse = restTemplate.exchange(photoUrl, HttpMethod.GET, entity,
                JsonNode.class);

        Integer resPhotoStatus = photoResponse.getStatusCode().value();
        System.out.println(resPhotoStatus);
        System.out.println("lollllll");
        if (resPhotoStatus != 200) {
            String[] message = { "Photo not found with id: " + photoId };
            ApiResponse response = new ApiResponse(false, message);
            return ResponseEntity.status(404).body(response);
        }

        // List<Comment> comments = commentRepo.findByPhotoId(photoId);
        // get only 2 newest comments
        List<Comment> comments = commentRepo.findTop2ByPhotoIdOrderByDateDesc(photoId);
        if (comments.isEmpty()) {
            String[] message = { "No comments found" };
            ApiResponse response = new ApiResponse(true, message, null);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            String[] message = { "Comments found" };
            List<Map<String, Object>> commentsData = new ArrayList<>();
            for (Comment comment : comments) {
                Integer userId = comment.getUserId();
                // call user API to get user data
                String userUrl = "http://localhost:8080/api/user/" + userId;
                HttpHeaders headers2 = new HttpHeaders();
                headers2.add("Authorization", "Bearer " + accessToken);

                // create a new HttpEntity object with the headers
                HttpEntity<String> entity2 = new HttpEntity<String>("parameters", headers2);
                ResponseEntity<JsonNode> userResponse = restTemplate.exchange(userUrl, HttpMethod.GET, entity2,
                        JsonNode.class);
                JsonNode userJson = userResponse.getBody();

                System.out.println(userJson);
                JsonNode userData = userJson.get("data");
                // extract username from user data
                String username = userData.get("username").asText();

                // create a map to hold comment data
                Map<String, Object> commentMap = new HashMap<>();
                commentMap.put("id", comment.getId());
                commentMap.put("comment", comment.getComment());
                commentMap.put("commentDate", comment.getDate());
                commentMap.put("photoId", comment.getPhotoId());
                commentMap.put("userId", userId);
                commentMap.put("username", username);

                commentsData.add(commentMap);
            }
            ApiResponse response = new ApiResponse(true, message, commentsData);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }

    // find all comments of a photo
    public ResponseEntity<?> getAllCommentsAndUsername(Long photoId, HttpServletRequest servletRequest) {
        String accessToken = servletRequest.getHeader("Authorization").substring(7);
        Optional<Photo> photoOptional = photoRepo.findById(photoId);
        if (!photoOptional.isPresent()) {
            String[] message = { "Photo not found with id: " + photoId };
            ApiResponse response = new ApiResponse(false, message);
            return ResponseEntity.status(404).body(response);
        }

        List<Comment> comments = commentRepo.findByPhotoIdOrderByDateDesc(photoId);
        if (comments.isEmpty()) {
            String[] message = { "No comments found" };
            ApiResponse response = new ApiResponse(true, message, null);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            String[] message = { "Comments found" };
            List<Map<String, Object>> commentsData = new ArrayList<>();
            for (Comment comment : comments) {
                Integer userId = comment.getUserId();
                // call user API to get user data
                String userUrl = "http://localhost:8080/api/user/" + userId;
                HttpHeaders headers = new HttpHeaders();
                headers.add("Authorization", "Bearer " + accessToken);

                // create a new HttpEntity object with the headers
                HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
                ResponseEntity<JsonNode> userResponse = restTemplate.exchange(userUrl, HttpMethod.GET, entity,
                        JsonNode.class);
                JsonNode userJson = userResponse.getBody();

                System.out.println(userJson);
                JsonNode userData = userJson.get("data");
                // extract username from user data
                String username = userData.get("username").asText();

                // create a map to hold comment data
                Map<String, Object> commentMap = new HashMap<>();
                commentMap.put("id", comment.getId());
                commentMap.put("comment", comment.getComment());
                commentMap.put("commentDate", comment.getDate());

                commentMap.put("photoId", comment.getPhotoId());
                commentMap.put("userId", userId);
                commentMap.put("username", username);

                commentsData.add(commentMap);
            }
            ApiResponse response = new ApiResponse(true, message, commentsData);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }

    public ResponseEntity<?> removeOne(Long id, HttpServletRequest request) {
        // if successfully deleted, return with 200 status and success message and
        // success true
        // else return with 400 status
        // get user id from request
        String token = request.getHeader("Authorization").substring(7);
        JwtService jwtService = new JwtService();
        Claims claims = jwtService.extractAllClaims(token);
        Integer userId = (Integer) claims.get("userId");
        // if the comment belongs to the user, delete it
        try {
            // find comment by id
            Optional<Comment> commentOptional = commentRepo.findById(id);
            // if comment not found, return with 404 status
            if (!commentOptional.isPresent()) {
                String[] message = { "Comment not found with id: " + id };
                ApiResponse response = new ApiResponse(false, message);
                // return with above message and 404 status
                return ResponseEntity.status(404).body(response);
            }
            if (commentOptional.get().getUserId() != userId) {
                String[] message = { "You are not authorized to delete this comment" };
                ApiResponse response = new ApiResponse(false, message);
                // return with above message and 404 status
                return ResponseEntity.status(404).body(response);
            }
            commentRepo.deleteById(id);
            String[] message = { "Comment deleted successfully" };
            ApiResponse response = new ApiResponse(false, message);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            String[] message = { "Comment not deleted" };
            ApiResponse responseBody = new ApiResponse(false, message);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
        }
    }

    public ResponseEntity<?> removeAllByPhotoId(Long photoId, HttpServletRequest request) {

        String token = request.getHeader("Authorization").substring(7);
        JwtService jwtService = new JwtService();
        Claims claims = jwtService.extractAllClaims(token);
        Integer userId = (Integer) claims.get("userId");
        // check if photo exists and belongs to user by calling /api/photos/details/id
        String photoUrl = "http://localhost:8080/api/photos/details/" + photoId;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        // call the api
        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
        ResponseEntity<JsonNode> photoResponse = restTemplate.exchange(photoUrl, HttpMethod.GET, entity,
                JsonNode.class);
        JsonNode photoJson = photoResponse.getBody();
        if (photoJson.get("success").asBoolean() == false) {
            String[] message = { "Photo not found with id: " + photoId };
            ApiResponse response = new ApiResponse(false, message);
            return ResponseEntity.status(404).body(response);
        }
        JsonNode photoData = photoJson.get("data");
        Integer photoUserId = photoData.get("userId").asInt();
        if (photoUserId != userId) {
            String[] message = { "You are not authorized to delete all comments on the post" };
            ApiResponse response = new ApiResponse(false, message);
            // return with above message and 404 status
            return ResponseEntity.status(403).body(response);
        }
        // if the photos belongs to the user, delete it
        try {
            // find comment by id
            List<Comment> comments = commentRepo.findByPhotoId(photoId);
            // if comment not found, return with 404 status
            if (comments.isEmpty()) {
                String[] message = { "Comments not found with photo id: " + photoId };
                ApiResponse response = new ApiResponse(true, message);
                // return with above message and 404 status
                return ResponseEntity.status(200).body(response);
            }

            commentRepo.deleteByPhotoId(photoId);
            String[] message = { "All comments deleted successfully" };
            ApiResponse response = new ApiResponse(false, message);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            String[] message = { "Comments not deleted" };
            ApiResponse responseBody = new ApiResponse(false, message);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
        }

    }

}
