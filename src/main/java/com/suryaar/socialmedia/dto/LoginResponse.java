
package com.suryaar.socialmedia.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing the authentication token and its validity information")
public class LoginResponse {

    @Schema(
            description = "JWT access token used to authenticate subsequent API requests",
            example = "eyJhbGciOiJIUzI1NiJ9..."
    )
    private String accessToken;

    @Schema(
            description = "Authentication token type",
            example = "Bearer"
    )
    private String tokenType;

    @Schema(
            description = "Access token validity duration in seconds",
            example = "3600"
    )
    private long expiresIn;

    public LoginResponse(String accessToken, String tokenType, long expiresIn) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }
}
