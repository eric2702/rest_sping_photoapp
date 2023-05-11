package com.soa_rest.helpers.response;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ApiResponse {
    // timestamp in this format "timestamp": "2023-05-01T15:14:49.635+00:00",
    private String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(new Date());
    private boolean success;
    private Object message;
    private Object data;

    public ApiResponse(boolean status, Object message, Object data) {
        this.success = status;
        this.message = message;
        this.data = Optional.ofNullable(data).orElse(new Object[0]);
    }

    public ApiResponse(boolean status, Object message) {
        this.success = status;
        this.message = message;
        // this data is empty array
        this.data = new Object[0];
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public boolean getSuccess() {
        return success;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Object getMessage() {
        return message;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Object getData() {
        return data;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getTimestamp() {
        return timestamp;
    }

}
