package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.dto.CreatePostRequest;
import com.suryaar.socialmedia.dto.CreateUserRequest;
import com.suryaar.socialmedia.dto.LoginRequest;
import com.suryaar.socialmedia.entity.User;
import com.suryaar.socialmedia.entity.UserRole;
import com.suryaar.socialmedia.entity.UserStatus;
import com.suryaar.socialmedia.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    void getUsers_byAdmin_shouldReturnUsers() throws Exception {

        // Register user
        CreateUserRequest registerRequest = new CreateUserRequest();
        registerRequest.setUsername("admin");
        registerRequest.setEmail("admin1@example.com");
        registerRequest.setPhone("9876543230");
        registerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentifier("admin");
        loginRequest.setPassword("Pass@123");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String adminToken = objectMapper.readTree(loginResponse)
                .get("accessToken").asString();

        long userId = userRepository.findByUsernameAndStatus("admin", UserStatus.ACTIVE)
                .orElseThrow()
                .getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow();

        user.setRole(UserRole.ADMIN);
        userRepository.save(user);

        // Register user 1
        CreateUserRequest registerRequestA = new CreateUserRequest();
        registerRequestA.setUsername("comment1");
        registerRequestA.setEmail("comment01@example.com");
        registerRequestA.setPhone("9876543231");
        registerRequestA.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestA)))
                .andExpect(status().isCreated());

        // Login
        LoginRequest loginRequestA = new LoginRequest();
        loginRequestA.setIdentifier("comment1");
        loginRequestA.setPassword("Pass@123");

        String loginResponseA = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestA)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String tokenA = objectMapper.readTree(loginResponseA)
                .get("accessToken").asString();

        // Register user 2
        CreateUserRequest registerRequestB = new CreateUserRequest();
        registerRequestB.setUsername("comment2");
        registerRequestB.setEmail("comment02@example.com");
        registerRequestB.setPhone("9876543232");
        registerRequestB.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestB)))
                .andExpect(status().isCreated());

        // Login
        LoginRequest loginRequestB = new LoginRequest();
        loginRequestB.setIdentifier("comment1");
        loginRequestB.setPassword("Pass@123");

        String loginResponseB = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestB)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String tokenB = objectMapper.readTree(loginResponseB)
                .get("accessToken").asString();

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void getUsers_byNormalUser_shouldReturnForbidden() throws Exception {

        // Register user 1
        CreateUserRequest registerRequestA = new CreateUserRequest();
        registerRequestA.setUsername("comment1");
        registerRequestA.setEmail("comment01@example.com");
        registerRequestA.setPhone("9876543231");
        registerRequestA.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestA)))
                .andExpect(status().isCreated());

        // Login
        LoginRequest loginRequestA = new LoginRequest();
        loginRequestA.setIdentifier("comment1");
        loginRequestA.setPassword("Pass@123");

        String loginResponseA = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestA)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(loginResponseA)
                .get("accessToken").asString();

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPosts_byAdmin_shouldReturnPosts() throws Exception {

        // Register user
        CreateUserRequest registerRequest = new CreateUserRequest();
        registerRequest.setUsername("admin");
        registerRequest.setEmail("admin1@example.com");
        registerRequest.setPhone("9876543230");
        registerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentifier("admin");
        loginRequest.setPassword("Pass@123");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String adminToken = objectMapper.readTree(loginResponse)
                .get("accessToken").asString();

        long userId = userRepository.findByUsernameAndStatus("admin", UserStatus.ACTIVE)
                .orElseThrow()
                .getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow();

        user.setRole(UserRole.ADMIN);
        userRepository.save(user);

        // Register user 1
        CreateUserRequest registerRequestA = new CreateUserRequest();
        registerRequestA.setUsername("comment1");
        registerRequestA.setEmail("comment01@example.com");
        registerRequestA.setPhone("9876543231");
        registerRequestA.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestA)))
                .andExpect(status().isCreated());

        // Login
        LoginRequest loginRequestA = new LoginRequest();
        loginRequestA.setIdentifier("comment1");
        loginRequestA.setPassword("Pass@123");

        String loginResponseA = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestA)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String tokenA = objectMapper.readTree(loginResponseA)
                .get("accessToken").asString();

        // Create two posts using userToken.

        // Create text post
        CreatePostRequest postRequest1 = new CreatePostRequest();
        postRequest1.setText("My first test post");
        postRequest1.setMediaIds(List.of());

        mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text")
                        .value("My first test post"));

        // Create text post
        CreatePostRequest postRequest2 = new CreatePostRequest();
        postRequest2.setText("My second test post");
        postRequest2.setMediaIds(List.of());

        mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text")
                        .value("My second test post"));

        mockMvc.perform(get("/api/admin/posts")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getPosts_byNormalUser_shouldReturnForbidden() throws Exception {

        // Register user 1
        CreateUserRequest registerRequestA = new CreateUserRequest();
        registerRequestA.setUsername("comment1");
        registerRequestA.setEmail("comment01@example.com");
        registerRequestA.setPhone("9876543231");
        registerRequestA.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestA)))
                .andExpect(status().isCreated());

        // Login
        LoginRequest loginRequestA = new LoginRequest();
        loginRequestA.setIdentifier("comment1");
        loginRequestA.setPassword("Pass@123");

        String loginResponseA = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestA)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(loginResponseA)
                .get("accessToken").asString();

        mockMvc.perform(get("/api/admin/posts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUsers_withoutAuthentication_shouldReturnUnauthorized()
            throws Exception {

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUsers_withInvalidPagination_shouldReturnBadRequest()
            throws Exception {

        // Register user
        CreateUserRequest registerRequest = new CreateUserRequest();
        registerRequest.setUsername("admin");
        registerRequest.setEmail("admin1@example.com");
        registerRequest.setPhone("9876543230");
        registerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentifier("admin");
        loginRequest.setPassword("Pass@123");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String adminToken = objectMapper.readTree(loginResponse)
                .get("accessToken").asString();

        long userId = userRepository.findByUsernameAndStatus("admin", UserStatus.ACTIVE)
                .orElseThrow()
                .getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow();

        user.setRole(UserRole.ADMIN);
        userRepository.save(user);

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "101"))
                .andExpect(status().isBadRequest());
    }
}