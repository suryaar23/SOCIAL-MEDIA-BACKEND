package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.dto.CreateUserRequest;
import com.suryaar.socialmedia.dto.LoginRequest;
import com.suryaar.socialmedia.entity.UserStatus;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FollowControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void followUser_shouldCreateRelationship() throws Exception {

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

        Long followerId = userRepository
                .findByUsernameAndStatus("follower1", UserStatus.ACTIVE)
                .orElseThrow().getUserId();

        Long targetId = userRepository
                .findByUsernameAndStatus("target01", UserStatus.ACTIVE)
                .orElseThrow().getUserId();

        assertEquals(1, followRepository.countByFollowing_UserId(targetId));
        assertTrue(followRepository.existsByFollower_UserIdAndFollowing_UserId(followerId, targetId));
    }

    @Test
    void followUser_twice_shouldCreateOnlyOneRelationship() throws Exception {

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

        // Follow target twice
        mockMvc.perform(post("/api/users/target01/follow")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/users/target01/follow")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        Long followerId = userRepository
                .findByUsernameAndStatus("follower1", UserStatus.ACTIVE)
                .orElseThrow().getUserId();

        Long targetId = userRepository
                .findByUsernameAndStatus("target01", UserStatus.ACTIVE)
                .orElseThrow().getUserId();

        assertEquals(1, followRepository.countByFollowing_UserId(targetId));
        assertTrue(followRepository.existsByFollower_UserIdAndFollowing_UserId(
                followerId, targetId));
    }

    @Test
    void unfollowUser_shouldRemoveFollowRelationship() throws Exception {

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

        //Follow
        mockMvc.perform(post("/api/users/target01/follow")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        //Unfollow
        mockMvc.perform(delete("/api/users/target01/follow")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        Long followerId = userRepository
                .findByUsernameAndStatus("follower1", UserStatus.ACTIVE)
                .orElseThrow().getUserId();

        Long targetId = userRepository
                .findByUsernameAndStatus("target01", UserStatus.ACTIVE)
                .orElseThrow().getUserId();

        assertEquals(0, followRepository.countByFollowing_UserId(targetId));
        assertFalse(followRepository.existsByFollower_UserIdAndFollowing_UserId(followerId, targetId));
    }

    @Test
    void followUser_shouldNotCreateRelationshipForSelfFollow() throws Exception {

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

        //Self-Follow attempt
        mockMvc.perform(post("/api/users/follower1/follow")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());

        Long followerId = userRepository
                .findByUsernameAndStatus("follower1", UserStatus.ACTIVE)
                .orElseThrow().getUserId();

        Long targetId = userRepository
                .findByUsernameAndStatus("target01", UserStatus.ACTIVE)
                .orElseThrow().getUserId();

        assertEquals(0, followRepository.countByFollowing_UserId(targetId));
        assertFalse(followRepository.existsByFollower_UserIdAndFollowing_UserId(followerId, targetId));
    }

    @Test
    void followUser_shouldNotCreateRelationshipForNonExistentUser() throws Exception {

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

        // Attempt to follow a non-existent user
        mockMvc.perform(post("/api/users/unknown_user/follow")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        Long followerId = userRepository
                .findByUsernameAndStatus("follower1", UserStatus.ACTIVE)
                .orElseThrow().getUserId();

        Long targetId = userRepository
                .findByUsernameAndStatus("target01", UserStatus.ACTIVE)
                .orElseThrow().getUserId();

        assertEquals(0, followRepository.countByFollowing_UserId(targetId));
        assertFalse(followRepository.existsByFollower_UserIdAndFollowing_UserId(followerId, targetId));
    }
}