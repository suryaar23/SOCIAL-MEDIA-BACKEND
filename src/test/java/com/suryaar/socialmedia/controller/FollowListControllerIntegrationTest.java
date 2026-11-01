package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.dto.CreateUserRequest;
import com.suryaar.socialmedia.dto.LoginRequest;
import com.suryaar.socialmedia.repository.FollowRepository;
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
class FollowListControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldReturn_FollowerList() throws Exception {

        // Register follower
        CreateUserRequest followerRequest = new CreateUserRequest();
        followerRequest.setUsername("follower1");
        followerRequest.setEmail("follower1@example.com");
        followerRequest.setPhone("9876543231");
        followerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(followerRequest)))
                .andExpect(status().isCreated());

        // Register target
        CreateUserRequest targetRequest = new CreateUserRequest();
        targetRequest.setUsername("target01");
        targetRequest.setEmail("target01@example.com");
        targetRequest.setPhone("9876543232");
        targetRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(targetRequest)))
                .andExpect(status().isCreated());

        // Login as follower
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentifier("follower1");
        loginRequest.setPassword("Pass@123");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(loginResponse)
                .get("accessToken").asString();

        // Follow target
        mockMvc.perform(post("/api/users/target01/follow")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/target01/followers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("follower1"));
    }

    @Test
    void shouldReturn_FollowingList() throws Exception {

        // Register follower
        CreateUserRequest followerRequest = new CreateUserRequest();
        followerRequest.setUsername("follower1");
        followerRequest.setEmail("follower1@example.com");
        followerRequest.setPhone("9876543231");
        followerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(followerRequest)))
                .andExpect(status().isCreated());

        // Register target
        CreateUserRequest targetRequest = new CreateUserRequest();
        targetRequest.setUsername("target01");
        targetRequest.setEmail("target01@example.com");
        targetRequest.setPhone("9876543232");
        targetRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(targetRequest)))
                .andExpect(status().isCreated());

        // Login as follower
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentifier("follower1");
        loginRequest.setPassword("Pass@123");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(loginResponse)
                .get("accessToken").asString();

        // Follow target
        mockMvc.perform(post("/api/users/target01/follow")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/follower1/following"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("target01"));
    }
}