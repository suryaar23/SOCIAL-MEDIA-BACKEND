package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.dto.CreatePostRequest;
import com.suryaar.socialmedia.dto.CreateUserRequest;
import com.suryaar.socialmedia.dto.LoginRequest;
import com.suryaar.socialmedia.entity.User;
import com.suryaar.socialmedia.entity.UserStatus;
import com.suryaar.socialmedia.repository.UserRepository;
import com.suryaar.socialmedia.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PublicPostsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    void getPublicPosts_shouldReturnUserPosts() throws Exception {

        // Register User A
        CreateUserRequest registerRequest = new CreateUserRequest();

        registerRequest.setUsername("postuser");
        registerRequest.setEmail("postuser@gmail.com");
        registerRequest.setPhone("9876543210");
        registerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());


        // Login as User A
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

        CreatePostRequest postRequest1 = new CreatePostRequest();
        postRequest1.setText("First post");
        postRequest1.setMediaIds(List.of());

        String postResponse1 = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest1)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long firstPostId = objectMapper.readTree(postResponse1)
                .get("postId").asLong();


        CreatePostRequest postRequest2 = new CreatePostRequest();
        postRequest2.setText("Second post");
        postRequest2.setMediaIds(List.of());

        String postResponse2 = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest2)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long secondPostId = objectMapper.readTree(postResponse2)
                .get("postId").asLong();

        mockMvc.perform(get("/api/users/postuser/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].username").value("postuser"))
                .andExpect(jsonPath("$.content[0].text").exists());
    }

    @Test
    void getPublicPosts_withoutAuthentication_shouldReturnPosts()
            throws Exception {

        // Register User A
        CreateUserRequest registerRequest = new CreateUserRequest();

        registerRequest.setUsername("postuser");
        registerRequest.setEmail("postuser@gmail.com");
        registerRequest.setPhone("9876543210");
        registerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login as User A
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

        CreatePostRequest postRequest1 = new CreatePostRequest();
        postRequest1.setText("First post");
        postRequest1.setMediaIds(List.of());

        String postResponse1 = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest1)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long firstPostId = objectMapper.readTree(postResponse1)
                .get("postId").asLong();

        mockMvc.perform(get("/api/users/postuser/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void getPublicPosts_whenUserHasNoPosts_shouldReturnEmptyPage()
            throws Exception {

        // Register User A
        CreateUserRequest registerRequest = new CreateUserRequest();

        registerRequest.setUsername("postuser");
        registerRequest.setEmail("postuser@gmail.com");
        registerRequest.setPhone("9876543210");
        registerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users/postuser/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getPublicPosts_withPagination_shouldReturnRequestedPage()
            throws Exception {

        // Register User A
        CreateUserRequest registerRequest = new CreateUserRequest();

        registerRequest.setUsername("postuser");
        registerRequest.setEmail("postuser@gmail.com");
        registerRequest.setPhone("9876543210");
        registerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());


        // Login as User A
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
                .get("accessToken").asText();

        // Create three posts as User A
        CreatePostRequest postRequest1 = new CreatePostRequest();
        postRequest1.setText("First post");
        postRequest1.setMediaIds(List.of());

        String postResponse1 = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest1)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long firstPostId = objectMapper.readTree(postResponse1)
                .get("postId").asLong();

        CreatePostRequest postRequest2 = new CreatePostRequest();
        postRequest2.setText("Second post");
        postRequest2.setMediaIds(List.of());

        String postResponse2 = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest2)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long secondPostId = objectMapper.readTree(postResponse2)
                .get("postId").asLong();


        CreatePostRequest postRequest3 = new CreatePostRequest();
        postRequest3.setText("Third post");
        postRequest3.setMediaIds(List.of());

        String postResponse3 = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest3)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long thirdPostId = objectMapper.readTree(postResponse3)
                .get("postId").asLong();

        mockMvc.perform(get("/api/users/postuser/posts")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    void getPublicPosts_withInvalidPagination_shouldReturnBadRequest()
            throws Exception {

        mockMvc.perform(get("/api/users/postuser/posts")
                        .param("page", "0")
                        .param("size", "51"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPublicPosts_withNonExistentUsername_shouldReturnNotFound()
            throws Exception {

        mockMvc.perform(get("/api/users/unknownuser/posts"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPublicPosts_shouldReturnNewestPostsFirst() throws Exception {

        // Register User A
        CreateUserRequest registerRequest = new CreateUserRequest();

        registerRequest.setUsername("postuser");
        registerRequest.setEmail("postuser@gmail.com");
        registerRequest.setPhone("9876543210");
        registerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login as User A
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
                .get("accessToken").asText();

        // Create 2 posts as User A
        CreatePostRequest postRequest1 = new CreatePostRequest();
        postRequest1.setText("First post");
        postRequest1.setMediaIds(List.of());

        String postResponse1 = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest1)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long firstPostId = objectMapper.readTree(postResponse1)
                .get("postId").asLong();


        CreatePostRequest postRequest2 = new CreatePostRequest();
        postRequest2.setText("Second post");
        postRequest2.setMediaIds(List.of());

        String postResponse2 = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest2)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long secondPostId = objectMapper.readTree(postResponse2)
                .get("postId").asLong();

        mockMvc.perform(get("/api/users/postuser/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].postId").value(secondPostId))
                .andExpect(jsonPath("$.content[1].postId").value(firstPostId));
    }

    @Test
    void getPublicPosts_fromInactiveUser_shouldReturnNotFound()
            throws Exception {

        // Register User A
        CreateUserRequest registerRequest = new CreateUserRequest();

        registerRequest.setUsername("postuser");
        registerRequest.setEmail("postuser@gmail.com");
        registerRequest.setPhone("9876543210");
        registerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Change User A's status to DELETED
        User user = userRepository.findByUsernameAndStatus(
                "postuser", UserStatus.ACTIVE
        ).orElseThrow();

        user.setStatus(UserStatus.DELETED);
        userRepository.saveAndFlush(user);

        mockMvc.perform(get("/api/users/postuser/posts"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPublicPosts_shouldReturnNullLikedByMe() throws Exception {

        // Register User A
        CreateUserRequest registerRequest = new CreateUserRequest();

        registerRequest.setUsername("postuser");
        registerRequest.setEmail("postuser@gmail.com");
        registerRequest.setPhone("9876543210");
        registerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());


        // Login as User A
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

        CreatePostRequest postRequest1 = new CreatePostRequest();
        postRequest1.setText("First post");
        postRequest1.setMediaIds(List.of());

        String postResponse1 = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest1)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long firstPostId = objectMapper.readTree(postResponse1)
                .get("postId").asLong();

        mockMvc.perform(get("/api/users/postuser/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].likedByMe").value(nullValue()));
    }
}