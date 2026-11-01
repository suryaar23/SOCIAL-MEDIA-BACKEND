package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.dto.CreateUserRequest;
import com.suryaar.socialmedia.dto.LoginRequest;
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
class PublicProfileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    void getPublicProfile_withoutAuthentication_shouldReturnProfile()
            throws Exception {

        // Register user
        CreateUserRequest registerRequest = new CreateUserRequest();
        registerRequest.setUsername("publicuser");
        registerRequest.setEmail("publicuser@example.com");
        registerRequest.setPhone("9876543230");
        registerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users/publicuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("publicuser"))
                .andExpect(jsonPath("$.followersCount").value(0))
                .andExpect(jsonPath("$.followingCount").value(0))
                .andExpect(jsonPath("$.isFollowing").doesNotExist());
    }

    @Test
    void getPublicProfile_withAuthentication_shouldReturnProfile()
            throws Exception {

        // Register user A
        CreateUserRequest registerRequest = new CreateUserRequest();
        registerRequest.setUsername("user1");
        registerRequest.setEmail("user1@example.com");
        registerRequest.setPhone("9876543230");
        registerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentifier("user1");
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

        // Register User B
        registerRequest = new CreateUserRequest();
        registerRequest.setUsername("publicuser");
        registerRequest.setEmail("publicuser@example.com");
        registerRequest.setPhone("9876543231");
        registerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users/publicuser")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("publicuser"))
                .andExpect(jsonPath("$.isFollowing").value(false));
    }

    @Test
    void getPublicProfile_whenViewerFollowsUser_shouldReturnFollowingTrue()
            throws Exception {

        // Register user A
        CreateUserRequest registerRequest = new CreateUserRequest();
        registerRequest.setUsername("user1");
        registerRequest.setEmail("user1@example.com");
        registerRequest.setPhone("9876543230");
        registerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentifier("user1");
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

        // Register User B
        registerRequest = new CreateUserRequest();
        registerRequest.setUsername("publicuser");
        registerRequest.setEmail("publicuser@example.com");
        registerRequest.setPhone("9876543231");
        registerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Follow target
        mockMvc.perform(post("/api/users/publicuser/follow")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/publicuser")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("publicuser"))
                .andExpect(jsonPath("$.followersCount").value(1))
                .andExpect(jsonPath("$.isFollowing").value(true));
    }

    @Test
    void getPublicProfile_withNonExistentUsername_shouldReturnNotFound()
            throws Exception {

        mockMvc.perform(get("/api/users/unknownuser"))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchUsers_withMatchingUsername_shouldReturnResults()
            throws Exception {

        CreateUserRequest user1 = new CreateUserRequest();
        user1.setUsername("srchusr1");
        user1.setEmail("searchuser1@gmail.com");
        user1.setPhone("9876543210");
        user1.setPassword("Pass1234");

        CreateUserRequest user2 = new CreateUserRequest();
        user2.setUsername("srchusr2");
        user2.setEmail("searchuser2@gmail.com");
        user2.setPhone("9876543211");
        user2.setPassword("Pass1234");

        CreateUserRequest user3 = new CreateUserRequest();
        user3.setUsername("another1");
        user3.setEmail("anotheruser@gmail.com");
        user3.setPhone("9876543212");
        user3.setPassword("Pass1234");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user3)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users/search")
                        .param("username", "srchusr"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].username")
                        .value("srchusr1"))
                .andExpect(jsonPath("$.content[1].username")
                        .value("srchusr2"));
    }

    @Test
    void searchUsers_shouldBeCaseInsensitive() throws Exception {

        CreateUserRequest user = new CreateUserRequest();
        user.setUsername("srchusr1");
        user.setEmail("searchuser1@gmail.com");
        user.setPhone("9876543210");
        user.setPassword("Pass1234");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users/search")
                        .param("username", "SRCHUSR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].username").value("srchusr1"));
    }

    @Test
    void searchUsers_withPagination_shouldReturnRequestedPage()
            throws Exception {

        CreateUserRequest user1 = new CreateUserRequest();
        user1.setUsername("pageuser1");
        user1.setEmail("pageuser1@gmail.com");
        user1.setPhone("9876543210");
        user1.setPassword("Passwor1");

        CreateUserRequest user2 = new CreateUserRequest();
        user2.setUsername("pageuser2");
        user2.setEmail("pageuser2@gmail.com");
        user2.setPhone("9876543211");
        user2.setPassword("Passwor1");

        CreateUserRequest user3 = new CreateUserRequest();
        user3.setUsername("pageuser3");
        user3.setEmail("pageuser3@gmail.com");
        user3.setPhone("9876543212");
        user3.setPassword("Passwor1");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user3)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users/search")
                        .param("username", "pageuser")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    void searchUsers_withInvalidPagination_shouldReturnBadRequest()
            throws Exception {

        mockMvc.perform(get("/api/users/search")
                        .param("username", "user")
                        .param("page", "0")
                        .param("size", "101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchUsers_withEmptyUsername_shouldReturnBadRequest()
            throws Exception {

        mockMvc.perform(get("/api/users/search")
                        .param("username", " "))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchUsers_withNoMatchingUsers_shouldReturnEmptyResults()
            throws Exception {

        mockMvc.perform(get("/api/users/search")
                        .param("username", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}