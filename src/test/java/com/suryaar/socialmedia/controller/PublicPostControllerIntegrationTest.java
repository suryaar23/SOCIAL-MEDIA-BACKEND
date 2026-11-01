package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.dto.CreatePostRequest;
import com.suryaar.socialmedia.dto.CreateUserRequest;
import com.suryaar.socialmedia.dto.LoginRequest;
import com.suryaar.socialmedia.entity.User;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PublicPostControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    void getPublicPost_withoutAuthentication_shouldReturnPost()
            throws Exception {

        // Register user
        CreateUserRequest registerRequest = new CreateUserRequest();
        registerRequest.setUsername("postuser");
        registerRequest.setEmail("postuser@example.com");
        registerRequest.setPhone("9876543230");
        registerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentifier("postuser");
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

        // Create post
        CreatePostRequest postRequest = new CreatePostRequest();
        postRequest.setText("My public post");

        String postResponse = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long postId = objectMapper.readTree(postResponse)
                .get("postId").asLong();

        mockMvc.perform(get("/api/posts/" + postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(postId))
                .andExpect(jsonPath("$.username").value("postuser"))
                .andExpect(jsonPath("$.text").value("My public post"));
    }

    @Test
    void getPublicPost_withAuthentication_shouldReturnPost()
            throws Exception {

        // Register user
        CreateUserRequest registerRequest = new CreateUserRequest();
        registerRequest.setUsername("postuserA");
        registerRequest.setEmail("postuserA@example.com");
        registerRequest.setPhone("9876543230");
        registerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentifier("postuserA");
        loginRequest.setPassword("Pass@123");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String tokenA = objectMapper.readTree(loginResponse)
                .get("accessToken").asString();

        // Register user 2
        registerRequest = new CreateUserRequest();
        registerRequest.setUsername("postuserB");
        registerRequest.setEmail("postuserB@example.com");
        registerRequest.setPhone("9876543231");
        registerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login as user 2
        loginRequest = new LoginRequest();
        loginRequest.setIdentifier("postuserB");
        loginRequest.setPassword("Pass@123");

        loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String tokenB = objectMapper.readTree(loginResponse)
                .get("accessToken").asString();

        // Create post
        CreatePostRequest postRequest = new CreatePostRequest();
        postRequest.setText("Testing Public post view with authentication");

        String postResponse = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long postId = objectMapper.readTree(postResponse)
                .get("postId").asLong();

        mockMvc.perform(get("/api/posts/" + postId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(postId))
                .andExpect(jsonPath("$.username").value("postuserB"))
                .andExpect(jsonPath("$.likedByMe").value(false));
    }

    @Test
    void getPublicPost_whenViewerLikedPost_shouldReturnLikedTrue()
            throws Exception {

        CreateUserRequest userARequest = new CreateUserRequest();

        userARequest.setUsername("likeUserA");
        userARequest.setEmail("likeUserA@example.com");
        userARequest.setPhone("9876543240");
        userARequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userARequest)))
                .andExpect(status().isCreated());

        LoginRequest loginA = new LoginRequest();
        loginA.setIdentifier("likeUserA");
        loginA.setPassword("Pass@123");

        String loginAResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginA)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String tokenA = objectMapper.readTree(loginAResponse)
                .get("accessToken").asText();

        CreateUserRequest userBRequest = new CreateUserRequest();

        userBRequest.setUsername("likeUserB");
        userBRequest.setEmail("likeUserB@example.com");
        userBRequest.setPhone("9876543241");
        userBRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userBRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginB = new LoginRequest();
        loginB.setIdentifier("likeUserB");
        loginB.setPassword("Pass@123");

        String loginBResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginB)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String tokenB = objectMapper.readTree(loginBResponse)
                .get("accessToken").asText();

        // Create post
        CreatePostRequest postRequest = new CreatePostRequest();
        postRequest.setText("Testing Public post view with authentication");

        String postResponse = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long postId = objectMapper.readTree(postResponse)
                .get("postId").asLong();

        mockMvc.perform(post("/api/posts/" + postId + "/like")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/posts/" + postId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likedByMe").value(true));
    }

    @Test
    void getPublicPost_withNonExistentId_shouldReturnNotFound()
            throws Exception {

        mockMvc.perform(get("/api/posts/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPublicPost_fromInactiveUser_shouldReturnNotFound()
            throws Exception {

        // Register User A.
        CreateUserRequest registerRequest = new CreateUserRequest();

        registerRequest.setUsername("postUserA");
        registerRequest.setEmail("postUserA@example.com");
        registerRequest.setPhone("9876543230");
        registerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login User A.
        LoginRequest loginRequest = new LoginRequest();

        loginRequest.setIdentifier("postUserA");
        loginRequest.setPassword("Pass@123");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(loginResponse)
                .get("accessToken").asText();

        // Create a post as User A.
        CreatePostRequest postRequest = new CreatePostRequest();

        postRequest.setText("Testing inactive user post");

        String postResponse = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long postId = objectMapper.readTree(postResponse)
                .get("postId").asLong();

        // Change User A's status to DELETED.
        User user = userRepository.findByUsernameAndStatus(
                "postUserA", UserStatus.ACTIVE
        ).orElseThrow();

        user.setStatus(UserStatus.DELETED);
        userRepository.saveAndFlush(user);

        // Verify public post is no longer accessible.
        mockMvc.perform(get("/api/posts/" + postId))
                .andExpect(status().isNotFound());
    }
}