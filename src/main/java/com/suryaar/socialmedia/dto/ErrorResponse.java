package com.suryaar.socialmedia.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "Standard API error response containing the HTTP status, message and validation errors")
public class ErrorResponse {

    @Schema(
            description = "HTTP status code associated with the error",
            example = "400"
    )
    private int status;

    @Schema(
            description = "General description of the error",
            example = "Validation failed"
    )
    private String message;

    @Schema(
            description = "Map containing field names and their corresponding validation error messages",
            example = "{\"username\":\"Username is required\",\"email\":\"Email should be valid\"}",
            nullable = true
    )
    private Map<String, String> errors;

    public ErrorResponse(int status, String message, Map<String, String> errors) {
        this.status = status;
        this.message = message;
        this.errors = errors;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
