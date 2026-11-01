package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.dto.*;
import com.suryaar.socialmedia.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PostControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostMediaRepository postMediaRepository;

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    void createTextPost() throws Exception {

        // Register and login user
        CreateUserRequest userRequest = new CreateUserRequest();
        userRequest.setUsername("postuser1");
        userRequest.setEmail("postuser1@gmail.com");
        userRequest.setPhone("9876543210");
        userRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentifier("postuser1");
        loginRequest.setPassword("Pass@123");

        String token = objectMapper.readTree(
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString()
        ).get("accessToken").asText();

        // Create text post
        CreatePostRequest postRequest = new CreatePostRequest();
        postRequest.setText("My first integration test post");
        postRequest.setMediaIds(List.of());

        mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text")
                        .value("My first integration test post"));
    }

    @Test
    void createMediaOnlyPost() throws Exception {

        // Register and login user
        CreateUserRequest userRequest = new CreateUserRequest();
        userRequest.setUsername("mediauser1");
        userRequest.setEmail("mediauser1@gmail.com");
        userRequest.setPhone("9876543210");
        userRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentifier("mediauser1");
        loginRequest.setPassword("Pass@123");

        String token = objectMapper.readTree(
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString()
        ).get("accessToken").asText();

        // Upload image
        MockMultipartFile image = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                Base64.getDecoder().decode(
                        "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////2wBDAf//////////////////////////////////////////////////////////////////////////////////////wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAb/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIQAxAAAAH/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAEBAAEFAqf/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAEDAQE/AT//xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAECAQE/AT//2Q=="
                )
        );

        String uploadResponse = mockMvc.perform(multipart("/api/media")
                        .file(image)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long mediaId = objectMapper.readTree(uploadResponse)
                .get("mediaId").asLong();

        // Create media-only post
        CreatePostRequest postRequest = new CreatePostRequest();
        postRequest.setMediaIds(List.of(mediaId));

        mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void createPostWithoutTextOrMediaShouldReturnBadRequest() throws Exception {

        // Register and login user
        CreateUserRequest userRequest = new CreateUserRequest();
        userRequest.setUsername("postuser2");
        userRequest.setEmail("postuser2@gmail.com");
        userRequest.setPhone("9876543211");
        userRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentifier("postuser2");
        loginRequest.setPassword("Pass@123");

        String token = objectMapper.readTree(
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString()
        ).get("accessToken").asText();

        // Empty post request
        CreatePostRequest postRequest = new CreatePostRequest();
        postRequest.setText(null);
        postRequest.setMediaIds(List.of());

        mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPostUsingAnotherUsersMediaShouldReturnForbidden() throws Exception {

        // Register and login User A
        CreateUserRequest userARequest = new CreateUserRequest();
        userARequest.setUsername("mediaowner");
        userARequest.setEmail("mediaowner@gmail.com");
        userARequest.setPhone("9876543212");
        userARequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userARequest)))
                .andExpect(status().isCreated());

        LoginRequest loginA = new LoginRequest();
        loginA.setIdentifier("mediaowner");
        loginA.setPassword("Pass@123");

        String tokenA = objectMapper.readTree(
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginA)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString()
        ).get("accessToken").asText();

        // User A uploads image
        MockMultipartFile image = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                Base64.getDecoder().decode(
                        "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////2wBDAf//////////////////////////////////////////////////////////////////////////////////////wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAb/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIQAxAAAAH/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAEBAAEFAqf/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAEDAQE/AT//xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAECAQE/AT//2Q=="
                )
        );

        String uploadResponse = mockMvc.perform(multipart("/api/media")
                        .file(image)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long mediaId = objectMapper.readTree(uploadResponse)
                .get("mediaId").asLong();

        // Register and login User B
        CreateUserRequest userBRequest = new CreateUserRequest();
        userBRequest.setUsername("postowner");
        userBRequest.setEmail("postowner@gmail.com");
        userBRequest.setPhone("9876543213");
        userBRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userBRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginB = new LoginRequest();
        loginB.setIdentifier("postowner");
        loginB.setPassword("Pass@123");

        String tokenB = objectMapper.readTree(
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginB)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString()).get("accessToken").asText();

        // User B tries to create a post using User A's media
        CreatePostRequest postRequest = new CreatePostRequest();
        postRequest.setText("Trying to use another user's media");
        postRequest.setMediaIds(List.of(mediaId));

        mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updatePostShouldAllowOwnerAndRejectAnotherUser() throws Exception {

        // Register and login User A
        CreateUserRequest userARequest = new CreateUserRequest();
        userARequest.setUsername("userA");
        userARequest.setEmail("updateuserA@gmail.com");
        userARequest.setPhone("9876543214");
        userARequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userARequest)))
                .andExpect(status().isCreated());

        LoginRequest loginA = new LoginRequest();
        loginA.setIdentifier("userA");
        loginA.setPassword("Pass@123");

        String tokenA = objectMapper.readTree(
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginA)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString()
        ).get("accessToken").asText();

        // User A creates a post
        CreatePostRequest createRequest = new CreatePostRequest();
        createRequest.setText("Original post");
        createRequest.setMediaIds(List.of());

        String postResponse = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long postId = objectMapper.readTree(postResponse)
                .get("postId").asLong();

        // User A updates their post
        UpdatePostRequest updateRequest = new UpdatePostRequest();
        updateRequest.setText("Updated post");

        mockMvc.perform(patch("/api/posts/" + postId)
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Updated post"));

        // Register and login User B
        CreateUserRequest userBRequest = new CreateUserRequest();
        userBRequest.setUsername("userB");
        userBRequest.setEmail("updateuserB@gmail.com");
        userBRequest.setPhone("9876543215");
        userBRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userBRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginB = new LoginRequest();
        loginB.setIdentifier("userB");
        loginB.setPassword("Pass@123");

        String tokenB = objectMapper.readTree(
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginB)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString()
        ).get("accessToken").asText();

        // User B tries to update User A's post
        UpdatePostRequest anotherUpdate = new UpdatePostRequest();
        anotherUpdate.setText("Unauthorized update");

        mockMvc.perform(patch("/api/posts/" + postId)
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(anotherUpdate)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deletePostShouldRemovePostAndAssociatedData() throws Exception {

        // Register and login User A
        CreateUserRequest userARequest = new CreateUserRequest();
        userARequest.setUsername("userA");
        userARequest.setEmail("deleteuserA@gmail.com");
        userARequest.setPhone("9876543216");
        userARequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userARequest)))
                .andExpect(status().isCreated());

        LoginRequest loginA = new LoginRequest();
        loginA.setIdentifier("userA");
        loginA.setPassword("Pass@123");

        String tokenA = objectMapper.readTree(
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginA)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString()
        ).get("accessToken").asText();

        // User A uploads image
        MockMultipartFile image = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                Base64.getDecoder().decode(
                        "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////2wBDAf//////////////////////////////////////////////////////////////////////////////////////wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAb/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIQAxAAAAH/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAEBAAEFAqf/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAEDAQE/AT//xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAECAQE/AT//2Q=="
                )
        );

        String uploadResponse = mockMvc.perform(multipart("/api/media")
                        .file(image)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long mediaId = objectMapper.readTree(uploadResponse)
                .get("mediaId").asLong();

        // User A creates a media-only post
        CreatePostRequest postRequest = new CreatePostRequest();
        postRequest.setMediaIds(List.of(mediaId));

        String postResponse = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long postId = objectMapper.readTree(postResponse)
                .get("postId").asLong();

        // Create a comment on the post
        CreateCommentRequest commentRequest = new CreateCommentRequest("Comment before deleting post");

        mockMvc.perform(post("/api/posts/" + postId + "/comments")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isCreated());

        // Register and login User B
        CreateUserRequest userBRequest = new CreateUserRequest();
        userBRequest.setUsername("userB");
        userBRequest.setEmail("deleteuserB@gmail.com");
        userBRequest.setPhone("9876543217");
        userBRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userBRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginB = new LoginRequest();
        loginB.setIdentifier("userB");
        loginB.setPassword("Pass@123");

        String tokenB = objectMapper.readTree(
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginB)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString()
        ).get("accessToken").asText();

        // User B likes User A's post
        mockMvc.perform(post("/api/posts/" + postId + "/like")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNoContent());

        // Verify associated data exists before deletion
        assertEquals(1, commentRepository.countByPostPostId(postId));
        assertEquals(1, likeRepository.countByPostPostId(postId));
        assertTrue(postMediaRepository.existsByMediaMediaId(mediaId));

        // User A deletes the post
        mockMvc.perform(delete("/api/posts/" + postId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());

        // Verify post and its associations are removed
        assertFalse(postRepository.existsById(postId));
        assertTrue(postMediaRepository
                .findByPostPostIdOrderByPositionAsc(postId).isEmpty());
        assertEquals(0, commentRepository.countByPostPostId(postId));
        assertEquals(0, likeRepository.countByPostPostId(postId));
        assertFalse(mediaRepository.existsById(mediaId));
    }
}