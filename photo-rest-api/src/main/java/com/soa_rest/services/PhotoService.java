package com.soa_rest.services;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soa_rest.config.JwtService;
import com.soa_rest.dto.PhotoUploadDTO;
import com.soa_rest.helpers.response.ApiResponse;
import com.soa_rest.models.entities.Photo;
import com.soa_rest.models.repos.PhotoRepo;
import com.soa_rest.util.ImageUtil;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

@Service
@Transactional
public class PhotoService {

    @Autowired
    private PhotoRepo photoRepo;

    @Autowired
    private RestTemplate restTemplate;

    public ResponseEntity uploadFile(PhotoUploadDTO request, HttpServletRequest servletRequest)
            throws IllegalStateException, IOException {
        String token = servletRequest.getHeader("Authorization").substring(7);
        JwtService jwtService = new JwtService();
        Claims claims = jwtService.extractAllClaims(token);
        Integer userId = (Integer) claims.get("userId");
        // Generate a unique ID for the uploaded file
        UUID uuid = UUID.randomUUID();
        // get the file extension
        String fileExtension = request.getFile().getOriginalFilename().split("\\.")[1];
        String fileName = uuid.toString() + "." + fileExtension;

        // Save the file to project directory
        // String appPath = new File(".").getCanonicalPath();
        // String uploadDirPath = appPath + File.separator + "uploads";
        String uploadDirPath = "C:\\xampp\\htdocs\\" + "photos";
        File uploadDir = new File(uploadDirPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        request.getFile().transferTo(new File(uploadDirPath + File.separator + fileName));

        // Save the filename and description to the database
        Photo photo = new Photo();
        photo.setName(fileName);
        photo.setDescription(request.getDescription());
        photo.setUserId(userId);
        photoRepo.save(photo);

        // Return a response with the UUID of the uploaded file
        String[] message = { "Post uploaded successfully" };
        ApiResponse response = new ApiResponse(true, message, photo);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> getOnePhotoAndAllComments(Long id, HttpServletRequest servletRequest) {
        String accessToken = servletRequest.getHeader("Authorization").substring(7);
        JwtService jwtService = new JwtService();
        Claims claims = jwtService.extractAllClaims(accessToken);
        Integer currentUserId = (Integer) claims.get("userId");
        String prefixLink = "http://localhost/photos/";
        Optional<Photo> photoOptional = photoRepo.findById(id);
        if (photoOptional.isPresent()) {
            Photo photo = photoOptional.get();
            String photoLink = prefixLink + photo.getName();
            Integer postUserId = photo.getUserId();

            // create a new HTTP headers object and add the authorization bearer
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + accessToken);

            // create a new HttpEntity object with the headers
            HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
            // get user details from user api
            String userUrl = "http://localhost:8080/api/user/" + postUserId;
            ResponseEntity<JsonNode> userResponse = restTemplate.exchange(userUrl, HttpMethod.GET, entity,
                    JsonNode.class);
            JsonNode userJson = userResponse.getBody();
            String postUsername = userJson.get("data").get("username").asText();

            // make a GET request to the comments service
            ResponseEntity<JsonNode> commentsResponse = restTemplate.exchange(
                    "http://localhost:8080/api/comments/all/" + id,
                    HttpMethod.GET, entity, JsonNode.class);

            JsonNode commentsJson = commentsResponse.getBody();
            JsonNode commentsData = commentsJson.get("data");
            // get all the comments
            List<Map<String, Object>> comments = new ArrayList<>();
            for (JsonNode comment : commentsData) {
                // only get comment text, userid, and username
                JsonNode commentText = comment.get("comment");
                JsonNode commentUserId = comment.get("userId");
                JsonNode commentUsername = comment.get("username");
                String commentDate = comment.get("commentDate").asText();
                // put in map and add to list
                Map<String, Object> commentMap = new HashMap<>();
                commentMap.put("comment", commentText);

                commentMap.put("commentId", comment.get("id").asLong());
                commentMap.put("userId", commentUserId);
                commentMap.put("commentDate", commentDate);

                commentMap.put("username", commentUsername);
                comments.add(commentMap);

            }

            // create a new map to store the photo and comments
            Map<String, Object> photoData = new HashMap<>();
            photoData.put("photoLink", photoLink);
            photoData.put("photoDate", photo.getDate());
            photoData.put("photoDescription", photo.getDescription());
            photoData.put("commentsSize", comments.size());
            photoData.put("photoId", photo.getId());
            photoData.put("postUsername", postUsername);
            photoData.put("postUserId", postUserId);
            if (currentUserId == postUserId) {
                photoData.put("isOwner", true);
            } else {
                photoData.put("isOwner", false);
            }
            photoData.put("comments", comments);

            // return the photo data
            String[] message = { "Photo found" };
            ApiResponse response = new ApiResponse(true, message, photoData);
            return ResponseEntity.ok(response);
        } else {
            String[] message = { "Photo not found with id: " + id };
            ApiResponse response = new ApiResponse(false, message);
            // return with above message and 404 status
            return ResponseEntity.status(404).body(response);
        }
    }

    public ResponseEntity<?> findOne(Long id) {
        Optional<Photo> photoOptional = photoRepo.findById(id);
        if (photoOptional.isPresent()) {
            Photo photo = photoOptional.get();
            String[] messages = { "Photo found" };

            ApiResponse response = new ApiResponse(true, messages, photo);
            return ResponseEntity.ok(response);
        } else {
            String[] message = { "Photo not found with id: " + id };
            ApiResponse response = new ApiResponse(false, message);
            // return with above message and 404 status
            return ResponseEntity.status(404).body(response);
        }
    }

    public ResponseEntity<?> getAllPhotosAndSomeComments(HttpServletRequest servletRequest) {
        String accessToken = servletRequest.getHeader("Authorization").substring(7);
        JwtService jwtService = new JwtService();
        Claims claims = jwtService.extractAllClaims(accessToken);
        Integer currentUserId = (Integer) claims.get("userId");
        String prefixLink = "http://localhost/photos/";
        // get all photos descending by Date
        List<Photo> photos = photoRepo.findAllByOrderByDateDesc();

        List<Map<String, Object>> photoData = new ArrayList<>();
        for (Photo photo : photos) {
            String photoLink = prefixLink + photo.getName();
            Integer postUserId = photo.getUserId();

            // create a new HTTP headers object and add the authorization bearer
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + accessToken);

            // create a new HttpEntity object with the headers
            HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

            // get user details from user api
            String userUrl = "http://localhost:8080/api/user/" + postUserId;
            ResponseEntity<JsonNode> userResponse = restTemplate.exchange(userUrl, HttpMethod.GET, entity,
                    JsonNode.class);
            JsonNode userJson = userResponse.getBody();
            String postUsername = userJson.get("data").get("username").asText();

            // get comments for this photo from the comments API
            String commentsUrl = "http://localhost:8080/api/comments/some/" + photo.getId();
            ResponseEntity<JsonNode> commentsResponse = restTemplate.exchange(commentsUrl, HttpMethod.GET, entity,
                    JsonNode.class);
            JsonNode commentsJson = commentsResponse.getBody();
            // extract the relevant information from the commentsJson
            List<Map<String, Object>> commentsList = new ArrayList<>();
            if (commentsJson != null && commentsJson.get("success").asBoolean()) {
                JsonNode commentsData = commentsJson.get("data");
                for (JsonNode commentNode : commentsData) {
                    String comment = commentNode.get("comment").asText();
                    String commentDate = commentNode.get("commentDate").asText();
                    int userId = commentNode.get("userId").asInt();
                    String username = commentNode.get("username").asText();
                    // you may need to call another API to get the user's information based on the
                    // userId
                    // get user username from http://localhost:8080/api/users/{userId}
                    // create a map to hold the comment data
                    Map<String, Object> commentMap = new HashMap<>();
                    commentMap.put("comment", comment);
                    commentMap.put("commentDate", commentDate);

                    commentMap.put("commentId", commentNode.get("id").asLong());
                    commentMap.put("userId", userId);
                    commentMap.put("username", username);
                    commentsList.add(commentMap);
                }
            }

            // create a map to hold photo data and comments
            Map<String, Object> photoMap = new HashMap<>();
            photoMap.put("photoLink", photoLink);
            photoMap.put("photoId", photo.getId());
            photoMap.put("photoDate", photo.getDate());
            photoMap.put("photoDescription", photo.getDescription());
            photoMap.put("postUserId", postUserId);
            if (currentUserId == postUserId) {
                photoMap.put("isOwner", true);
            } else {
                photoMap.put("isOwner", false);
            }
            photoMap.put("postUsername", postUsername);
            photoMap.put("comments", commentsList);

            // get comments size from http://localhost:8080/api/comments/size/{photoId}
            ResponseEntity<JsonNode> commentsSizeResponse = restTemplate.exchange(
                    "http://localhost:8080/api/comments/size/" + photo.getId(),
                    HttpMethod.GET, entity, JsonNode.class);
            JsonNode commentsSizeJson = commentsSizeResponse.getBody();
            JsonNode commentsSizeData = commentsSizeJson.get("data").get("commentsSize");
            photoMap.put("commentsSize", commentsSizeData);

            photoData.add(photoMap);

        }
        String[] message = { "Photos and comments found" };
        ApiResponse response = new ApiResponse(true, message, photoData);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> removeOne(Long id, HttpServletRequest request) {
        // if successfully deleted, return with 200 status and success message and
        // success true
        // else return with 400 status
        String token = request.getHeader("Authorization").substring(7);
        JwtService jwtService = new JwtService();
        Claims claims = jwtService.extractAllClaims(token);
        Integer userId = (Integer) claims.get("userId");
        // if the photo belongs to the user, delete it
        try {
            // find photo by id
            Optional<Photo> photoOptional = photoRepo.findById(id);
            // if comment not found, return with 404 status
            if (!photoOptional.isPresent()) {
                String[] message = { "Photo not found with id: " + id };
                ApiResponse response = new ApiResponse(false, message);
                // return with above message and 404 status
                return ResponseEntity.status(404).body(response);
            }
            if (photoOptional.get().getUserId() != userId) {
                String[] message = { "You are not authorized to delete this photo" };
                ApiResponse response = new ApiResponse(false, message);
                // return with above message and 404 status
                return ResponseEntity.status(403).body(response);
            }
            // delete photo from file
            String photoName = photoOptional.get().getName();
            File photoFile = new File("C:\\xampp\\htdocs\\" + "photos\\" + photoName);

            if (photoFile.delete()) {
                System.out.println("File deleted successfully");
                // delete photo from database
                photoRepo.deleteById(id);
                // delete comments for this photo from the comments API
                // /api/comments/all/{photoId}
                String commentsUrl = "http://localhost:8080/api/comments/all/" + id;
                HttpHeaders headers = new HttpHeaders();
                headers.add("Authorization", "Bearer " + token);
                HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

                ResponseEntity<JsonNode> commentsResponse = restTemplate.exchange(commentsUrl, HttpMethod.DELETE,
                        entity,
                        JsonNode.class);
                JsonNode commentsJson = commentsResponse.getBody();
                String[] message = { "Photo and all comments deleted successfully" };
                ApiResponse response = new ApiResponse(true, message);
                return ResponseEntity.status(HttpStatus.OK).body(response);
                // if success, return with 200 status and success message and success true
            } else {
                System.out.println("Failed to delete the file");
                String[] message = { "Photo not deleted" };
                ApiResponse response = new ApiResponse(false, message);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } catch (Exception e) {
            String[] message = { "Photo not deleted" };
            ApiResponse responseBody = new ApiResponse(false, message);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
        }

    }

    public List<Photo> findByName(String name) {
        return photoRepo.findByNameContains(name);
    }
}
