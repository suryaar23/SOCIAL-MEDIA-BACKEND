package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.dto.ChangePasswordRequest;
import com.suryaar.socialmedia.dto.CreateUserRequest;
import com.suryaar.socialmedia.dto.LoginRequest;
import com.suryaar.socialmedia.entity.UserStatus;
import com.suryaar.socialmedia.repository.RevokedTokenRepository;
import com.suryaar.socialmedia.repository.UserRepository;
import com.suryaar.socialmedia.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.springframework.http.MediaType;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RevokedTokenRepository revokedTokenRepository;

    @Autowired
    private JwtService jwtService;

    @Test
    void registerUser_shouldReturnCreatedAndSaveUser() throws Exception {

        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("surya_01");
        request.setEmail("surya01@gmail.com");
        request.setPhone("9876543210");
        request.setPassword("pass1234");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.username").value("surya_01"))
                .andExpect(jsonPath("$.email").value("surya01@gmail.com"));

        assertTrue(userRepository.findByUsernameAndStatus(
                        "surya_01", UserStatus.ACTIVE).isPresent()
        );
    }

    @Test
    void registerUser_withDuplicateUsername_shouldReturnConflict() throws Exception {

        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("surya_01");
        request.setEmail("another@example.com");
        request.setPhone("9876543211");
        request.setPassword("Pass@123");

        // Register the user for the first time
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Attempt registration with the same username
        request.setEmail("another2@example.com");
        request.setPhone("9876543212");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Username is already taken"));
    }

    @Test
    void registerUser_withInvalidUsername_shouldReturnBadRequest() throws Exception {

        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("abc");
        request.setEmail("valid@example.com");
        request.setPhone("9876543213");
        request.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.username")
                        .value("Username must be between 5 and 10 characters"));
    }

    @Test
    void loginUser_withUsername_shouldReturnAccessToken() throws Exception {

        // Register a user
        CreateUserRequest registerRequest = new CreateUserRequest();
        registerRequest.setUsername("login_01");
        registerRequest.setEmail("login01@example.com");
        registerRequest.setPhone("9876543214");
        registerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Prepare login request
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentifier("login_01");
        loginRequest.setPassword("Pass@123");

        // Login
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").isNumber());
    }

    @Test
    void loginUser_withEmail_shouldReturnAccessToken() throws Exception {

        // Register a user
        CreateUserRequest registerRequest = new CreateUserRequest();
        registerRequest.setUsername("login_01");
        registerRequest.setEmail("login01@example.com");
        registerRequest.setPhone("9876543214");
        registerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Prepare login request
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentifier("login01@example.com");
        loginRequest.setPassword("Pass@123");

        // Login
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").isNumber());
    }

    @Test
    void loginUser_withIncorrectPassword_shouldReturnUnauthorized() throws Exception {

        // Register a user
        CreateUserRequest registerRequest = new CreateUserRequest();
        registerRequest.setUsername("wrong_01");
        registerRequest.setEmail("wrong01@example.com");
        registerRequest.setPhone("9876543216");
        registerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login with incorrect password
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentifier("wrong_01");
        loginRequest.setPassword("Wrong@12");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))

                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message")
                        .value("Invalid username/email or password"));
    }

    @Test
    void loginUser_withInvalidUsername_shouldReturnUnauthorized() throws Exception {

        // Register a user
        CreateUserRequest registerRequest = new CreateUserRequest();
        registerRequest.setUsername("wrong_01");
        registerRequest.setEmail("wrong01@example.com");
        registerRequest.setPhone("9876543216");
        registerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login with incorrect username
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentifier("user1");
        loginRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))

                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message")
                        .value("Invalid username/email or password"));
    }

    @Test
    void loginUser_withInvalidEmail_shouldReturnUnauthorized() throws Exception {

        // Register a user
        CreateUserRequest registerRequest = new CreateUserRequest();
        registerRequest.setUsername("wrong_01");
        registerRequest.setEmail("wrong01@example.com");
        registerRequest.setPhone("9876543216");
        registerRequest.setPassword("Pass@123");

        mockMvc.perform((post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest))))
                .andExpect(status().isCreated());

        // Login with incorrect email
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentifier("user01@gmail.com");
        loginRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))

                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message")
                        .value("Invalid username/email or password"));

    }

    @Test
    void logoutUser_withValidToken_shouldRevokeToken() throws Exception {

        // Register user
        CreateUserRequest registerRequest = new CreateUserRequest();
        registerRequest.setUsername("logout_01");
        registerRequest.setEmail("logout01@example.com");
        registerRequest.setPhone("9876543217");
        registerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login and obtain JWT
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentifier("logout_01");
        loginRequest.setPassword("Pass@123");

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(response)
                .get("accessToken").asString();

        String tokenId = jwtService.extractTokenId(token);

        // Logout
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // Verify that the token is revoked
        assertTrue(revokedTokenRepository.existsByTokenId(tokenId));
    }

    @Test
    void logoutUser_withoutToken_shouldReturnUnauthorized() throws Exception {

        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message")
                        .value("Authentication required"));
    }

    @Test
    void changePassword_withValidDetails_shouldUpdatePassword() throws Exception {

        // Register user
        CreateUserRequest registerRequest = new CreateUserRequest();
        registerRequest.setUsername("change_01");
        registerRequest.setEmail("change01@example.com");
        registerRequest.setPhone("9876543218");
        registerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentifier("change_01");
        loginRequest.setPassword("Pass@123");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(loginResponse)
                .get("accessToken").asString();

        // Change password
        ChangePasswordRequest changeRequest = new ChangePasswordRequest();
        changeRequest.setCurrentPassword("Pass@123");
        changeRequest.setNewPassword("NewPass1");
        changeRequest.setConfirmPassword("NewPass1");

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeRequest)))
                        .andExpect(status().isNoContent());

        // Attempt to use the old token after password change
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());

        // Verify old password no longer works
        LoginRequest oldLogin = new LoginRequest();
        oldLogin.setIdentifier("change_01");
        oldLogin.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(oldLogin)))
                .andExpect(status().isUnauthorized());

        // Verify new password works
        LoginRequest newLogin = new LoginRequest();
        newLogin.setIdentifier("change_01");
        newLogin.setPassword("NewPass1");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLogin)))
                .andExpect(status().isOk());
    }

    @Test
    void changePassword_withInvalidCurrentPassword_shouldReturnUnauthorized() throws Exception {

        // Register user
        CreateUserRequest registerRequest = new CreateUserRequest();
        registerRequest.setUsername("change_01");
        registerRequest.setEmail("change01@example.com");
        registerRequest.setPhone("9876543218");
        registerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentifier("change_01");
        loginRequest.setPassword("Pass@123");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String token = objectMapper.readTree(loginResponse)
                .get("accessToken").asString();

        ChangePasswordRequest changeRequest = new ChangePasswordRequest();
        changeRequest.setCurrentPassword("Wrong@12");
        changeRequest.setNewPassword("NewPass1");
        changeRequest.setConfirmPassword("NewPass1");

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeRequest)))
                        .andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.message")
                        .value("Current password is incorrect"));
    }

    @Test
    void changePassword_withMismatchedConfirmation_shouldReturnBadRequest() throws Exception {

        // Register user
        CreateUserRequest registerRequest = new CreateUserRequest();
        registerRequest.setUsername("change_01");
        registerRequest.setEmail("change01@example.com");
        registerRequest.setPhone("9876543218");
        registerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentifier("change_01");
        loginRequest.setPassword("Pass@123");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String token = objectMapper.readTree(loginResponse)
                .get("accessToken").asString();

        ChangePasswordRequest changeRequest = new ChangePasswordRequest();
        changeRequest.setCurrentPassword("Pass@123");
        changeRequest.setNewPassword("NewPass1");
        changeRequest.setConfirmPassword("Other@12");

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("New password and confirmation do not match"));
    }

    @Test
    void changePassword_withSamePassword_shouldReturnBadRequest() throws Exception {

        // Register user
        CreateUserRequest registerRequest = new CreateUserRequest();
        registerRequest.setUsername("change_01");
        registerRequest.setEmail("change01@example.com");
        registerRequest.setPhone("9876543218");
        registerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                        .andExpect(status().isCreated());

        // Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentifier("change_01");
        loginRequest.setPassword("Pass@123");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(loginResponse)
                .get("accessToken").asString();

        ChangePasswordRequest changeRequest = new ChangePasswordRequest();
        changeRequest.setCurrentPassword("Pass@123");
        changeRequest.setNewPassword("Pass@123");
        changeRequest.setConfirmPassword("Pass@123");

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeRequest)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                        .value("New password must differ from the current password"));
    }
}