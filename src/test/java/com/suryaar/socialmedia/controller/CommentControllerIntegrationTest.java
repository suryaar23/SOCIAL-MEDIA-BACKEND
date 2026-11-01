package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.dto.*;
import com.suryaar.socialmedia.repository.CommentRepository;
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
class CommentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    void createComment_withValidText_shouldReturnCreated() throws Exception {

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

        // Create post
        CreatePostRequest postRequest = new CreatePostRequest();
        postRequest.setText("Testing Comment functionality");

        String postResponse = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long postId = objectMapper.readTree(postResponse)
                .get("postId").asLong();

        // Create comment
        CreateCommentRequest request =
                new CreateCommentRequest("This is my first comment");

        String response = mockMvc.perform(
                        post("/api/posts/" + postId + "/comments")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.commentId").isNumber())
                .andExpect(jsonPath("$.username").value("comment1"))
                .andExpect(jsonPath("$.text")
                        .value("This is my first comment"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long commentId = objectMapper.readTree(response)
                .get("commentId")
                .asLong();

        assertTrue(commentRepository.existsById(commentId));
    }

    @Test
    void createComment_withBlankText_shouldReturnBadRequest() throws Exception {

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

        // Create post
        CreatePostRequest postRequest = new CreatePostRequest();
        postRequest.setText("Testing Comment functionality");

        String postResponse = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long postId = objectMapper.readTree(postResponse)
                .get("postId").asLong();

        CreateCommentRequest request = new CreateCommentRequest("   ");

        mockMvc.perform(post("/api/posts/" + postId + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getComments_shouldReturnCommentList() throws Exception {

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

        // Create post
        CreatePostRequest postRequest = new CreatePostRequest();
        postRequest.setText("Testing Comment functionality");

        String postResponse = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long postId = objectMapper.readTree(postResponse)
                .get("postId").asLong();

        CreateCommentRequest request =
                new CreateCommentRequest("First test comment");

        mockMvc.perform(post("/api/posts/" + postId + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/posts/" + postId + "/comments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].text")
                        .value("First test comment"));
    }

    @Test
    void getComments_withInvalidPageSize_shouldReturnBadRequest()
            throws Exception {

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

        // Create post
        CreatePostRequest postRequest = new CreatePostRequest();
        postRequest.setText("Testing Comment functionality");

        String postResponse = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long postId = objectMapper.readTree(postResponse)
                .get("postId").asLong();

        mockMvc.perform(get("/api/posts/" + postId + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateComment_byOwner_shouldReturnUpdatedComment() throws Exception {

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

        // Create post
        CreatePostRequest postRequest = new CreatePostRequest();
        postRequest.setText("Testing Comment functionality");

        String postResponse = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long postId = objectMapper.readTree(postResponse)
                .get("postId").asLong();

        CreateCommentRequest createRequest =
                new CreateCommentRequest("Original comment");

        String response = mockMvc.perform(
                        post("/api/posts/" + postId + "/comments")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long commentId = objectMapper.readTree(response)
                .get("commentId").asLong();

        UpdateCommentRequest updateRequest =
                new UpdateCommentRequest("Updated comment");

        mockMvc.perform(
                        patch("/api/posts/" + postId + "/comments/" + commentId)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Updated comment"));
    }

    @Test
    void updateComment_byNonOwner_shouldReturnForbidden() throws Exception {

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

        String tokenA = objectMapper.readTree(loginResponse)
                .get("accessToken").asString();

        // Create post
        CreatePostRequest postRequest = new CreatePostRequest();
        postRequest.setText("Testing Comment functionality");

        String postResponse = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long postId = objectMapper.readTree(postResponse)
                .get("postId").asLong();

        CreateCommentRequest createRequest =
                new CreateCommentRequest("User A's comment");

        String response = mockMvc.perform(
                        post("/api/posts/" + postId + "/comments")
                                .header("Authorization", "Bearer " + tokenA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long commentId = objectMapper.readTree(response)
                .get("commentId").asLong();

        // Register user 2
        registerRequest = new CreateUserRequest();
        registerRequest.setUsername("comment2");
        registerRequest.setEmail("comment02@example.com");
        registerRequest.setPhone("9876543231");
        registerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login as user 2
        loginRequest = new LoginRequest();
        loginRequest.setIdentifier("comment2");
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

        UpdateCommentRequest updateRequest =
                new UpdateCommentRequest("Trying to edit another user's comment");

        mockMvc.perform(patch("/api/posts/" + postId + "/comments/" + commentId)
                                .header("Authorization", "Bearer " + tokenB)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteComment_byOwner_shouldReturnNoContent() throws Exception {

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

        // Create post
        CreatePostRequest postRequest = new CreatePostRequest();
        postRequest.setText("Testing Comment functionality");

        String postResponse = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long postId = objectMapper.readTree(postResponse)
                .get("postId").asLong();

        CreateCommentRequest request =
                new CreateCommentRequest("Comment to delete");

        String response = mockMvc.perform(
                        post("/api/posts/" + postId + "/comments")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long commentId = objectMapper.readTree(response)
                .get("commentId").asLong();

        mockMvc.perform(
                        delete("/api/posts/" + postId + "/comments/" + commentId)
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertFalse(commentRepository.existsById(commentId));
    }

    @Test
    void deleteComment_byNonOwner_shouldReturnForbidden() throws Exception {

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

        String tokenA = objectMapper.readTree(loginResponse)
                .get("accessToken").asString();

        // Create post
        CreatePostRequest postRequest = new CreatePostRequest();
        postRequest.setText("Testing Comment functionality");

        String postResponse = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long postId = objectMapper.readTree(postResponse)
                .get("postId").asLong();

        CreateCommentRequest createRequest =
                new CreateCommentRequest("User A's comment");

        String response = mockMvc.perform(
                        post("/api/posts/" + postId + "/comments")
                                .header("Authorization", "Bearer " + tokenA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long commentId = objectMapper.readTree(response)
                .get("commentId").asLong();

        // Register user 2
        registerRequest = new CreateUserRequest();
        registerRequest.setUsername("comment2");
        registerRequest.setEmail("comment02@example.com");
        registerRequest.setPhone("9876543231");
        registerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login as user 2
        loginRequest = new LoginRequest();
        loginRequest.setIdentifier("comment2");
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

        mockMvc.perform(delete("/api/posts/" + postId + "/comments/" + commentId)
                                .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());

        assertTrue(commentRepository.existsById(commentId));
    }
}