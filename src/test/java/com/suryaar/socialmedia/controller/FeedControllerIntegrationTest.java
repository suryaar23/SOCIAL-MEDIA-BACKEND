package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.dto.CreatePostRequest;
import com.suryaar.socialmedia.dto.CreateUserRequest;
import com.suryaar.socialmedia.dto.LoginRequest;
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
class FeedControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getFeed_withNoFollowing_shouldReturnEmptyFeed() throws Exception {

        // Register user
        CreateUserRequest registerRequest = new CreateUserRequest();
        registerRequest.setUsername("comment1");
        registerRequest.setEmail("comment01@example.com");
        registerRequest.setPhone("9876543230");
        registerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentifier("comment1");
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

        mockMvc.perform(get("/api/users/me/feed")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getFeed_shouldReturnPostsFromFollowedUsers() throws Exception {

        // Register/login User A and obtain tokenA.
        CreateUserRequest userARequest = new CreateUserRequest();
        userARequest.setUsername("feeduserA");
        userARequest.setEmail("feeduserA@gmail.com");
        userARequest.setPhone("9876543210");
        userARequest.setPassword("password");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userARequest)))
                .andExpect(status().isCreated());

        LoginRequest loginA = new LoginRequest();
        loginA.setIdentifier("feeduserA");
        loginA.setPassword("password");

        String loginAResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginA)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String tokenA = objectMapper.readTree(loginAResponse)
                .get("accessToken").asText();

        // Register/login User B and obtain tokenB.
        CreateUserRequest userBRequest = new CreateUserRequest();
        userBRequest.setUsername("feeduserB");
        userBRequest.setEmail("feeduserB@gmail.com");
        userBRequest.setPhone("9876543211");
        userBRequest.setPassword("password");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userBRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginB = new LoginRequest();
        loginB.setIdentifier("feeduserB");
        loginB.setPassword("password");

        String loginBResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginB)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String tokenB = objectMapper.readTree(loginBResponse)
                .get("accessToken").asText();

        // User A follows User B.
        mockMvc.perform(post("/api/users/feeduserB/follow")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());

        // Create posts as User B.
        CreatePostRequest firstPostRequest = new CreatePostRequest();
        firstPostRequest.setText("First post");

        String firstPostResponse = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstPostRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long firstPostId = objectMapper.readTree(firstPostResponse)
                .get("postId").asLong();


        CreatePostRequest secondPostRequest = new CreatePostRequest();
        secondPostRequest.setText("Second post");

        String secondPostResponse = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondPostRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long secondPostId = objectMapper.readTree(secondPostResponse)
                .get("postId").asLong();

        mockMvc.perform(get("/api/users/me/feed")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].postId").value(secondPostId))
                .andExpect(jsonPath("$.content[1].postId").value(firstPostId));
    }

    @Test
    void getFeed_shouldExcludeOwnPosts() throws Exception {

        // Register/login User A and obtain tokenA.
        CreateUserRequest userARequest = new CreateUserRequest();
        userARequest.setUsername("feeduserA");
        userARequest.setEmail("feeduserA@gmail.com");
        userARequest.setPhone("9876543210");
        userARequest.setPassword("password");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userARequest)))
                .andExpect(status().isCreated());

        LoginRequest loginA = new LoginRequest();
        loginA.setIdentifier("feeduserA");
        loginA.setPassword("password");

        String loginAResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginA)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String tokenA = objectMapper.readTree(loginAResponse)
                .get("accessToken").asText();

        // Register/login User B and obtain tokenB.
        CreateUserRequest userBRequest = new CreateUserRequest();
        userBRequest.setUsername("feeduserB");
        userBRequest.setEmail("feeduserB@gmail.com");
        userBRequest.setPhone("9876543211");
        userBRequest.setPassword("password");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userBRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginB = new LoginRequest();
        loginB.setIdentifier("feeduserB");
        loginB.setPassword("password");

        String loginBResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginB)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String tokenB = objectMapper.readTree(loginBResponse)
                .get("accessToken").asText();

        // User A follows User B.
        mockMvc.perform(post("/api/users/feeduserB/follow")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());

        // Create posts as User A
        CreatePostRequest firstPostRequest = new CreatePostRequest();
        firstPostRequest.setText("User A post");

        String firstPostResponse = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstPostRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

//        Long firstPostId = objectMapper.readTree(firstPostResponse)
//                .get("postId").asLong();

        //Create posts as User B
        CreatePostRequest secondPostRequest = new CreatePostRequest();
        secondPostRequest.setText("Second post");

        String secondPostResponse = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondPostRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

//        Long secondPostId = objectMapper.readTree(secondPostResponse)
//                .get("postId").asLong();

        mockMvc.perform(get("/api/users/me/feed")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].username").value("feeduserB"));
    }

    @Test
    void getFeed_shouldReturnNewestPostsFirst() throws Exception {

        // Register/login User A and obtain tokenA.
        CreateUserRequest userARequest = new CreateUserRequest();
        userARequest.setUsername("feeduserA");
        userARequest.setEmail("feeduserA@gmail.com");
        userARequest.setPhone("9876543210");
        userARequest.setPassword("password");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userARequest)))
                .andExpect(status().isCreated());

        LoginRequest loginA = new LoginRequest();
        loginA.setIdentifier("feeduserA");
        loginA.setPassword("password");

        String loginAResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginA)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String tokenA = objectMapper.readTree(loginAResponse)
                .get("accessToken").asText();

        // Register/login User B and obtain tokenB.
        CreateUserRequest userBRequest = new CreateUserRequest();
        userBRequest.setUsername("feeduserB");
        userBRequest.setEmail("feeduserB@gmail.com");
        userBRequest.setPhone("9876543211");
        userBRequest.setPassword("password");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userBRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginB = new LoginRequest();
        loginB.setIdentifier("feeduserB");
        loginB.setPassword("password");

        String loginBResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginB)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String tokenB = objectMapper.readTree(loginBResponse)
                .get("accessToken").asText();

        // User A follows User B.
        mockMvc.perform(post("/api/users/feeduserB/follow")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());

        // Create 2 posts as User B
        CreatePostRequest firstPostRequest = new CreatePostRequest();
        firstPostRequest.setText("First post");

        String firstPostResponse = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstPostRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long firstPostId = objectMapper.readTree(firstPostResponse)
                .get("postId").asLong();

        CreatePostRequest secondPostRequest = new CreatePostRequest();
        secondPostRequest.setText("Second post");

        String secondPostResponse = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondPostRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long secondPostId = objectMapper.readTree(secondPostResponse)
                .get("postId").asLong();

        mockMvc.perform(get("/api/users/me/feed")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].postId").value(secondPostId))
                .andExpect(jsonPath("$.content[1].postId").value(firstPostId));
    }

    @Test
    void getFeed_withInvalidPageSize_shouldReturnBadRequest() throws Exception {

        // Register user
        CreateUserRequest registerRequest = new CreateUserRequest();
        registerRequest.setUsername("comment1");
        registerRequest.setEmail("comment01@example.com");
        registerRequest.setPhone("9876543230");
        registerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentifier("comment1");
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

        mockMvc.perform(get("/api/users/me/feed")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "51"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getFeed_withoutAuthentication_shouldReturnUnauthorized()
            throws Exception {

        mockMvc.perform(get("/api/users/me/feed"))
                .andExpect(status().isUnauthorized());
    }
}