package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.dto.CreatePostRequest;
import com.suryaar.socialmedia.dto.CreateUserRequest;
import com.suryaar.socialmedia.dto.LoginRequest;
import com.suryaar.socialmedia.repository.MediaRepository;
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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MediaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MediaRepository mediaRepository;

    @Test
    void uploadMedia_withValidImage_shouldReturnCreated() throws Exception {

        // Register user
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

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                Base64.getDecoder().decode(
                        "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////2wBDAf//////////////////////////////////////////////////////////////////////////////////////wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAb/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIQAxAAAAH/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAEBAAEFAqf/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAEDAQE/AT//xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAECAQE/AT//2Q=="
                )
        );

        String response = mockMvc.perform(
                        multipart("/api/media")
                                .file(file)
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mediaId").isNumber())
                .andExpect(jsonPath("$.name").value("test-image.jpg"))
                .andExpect(jsonPath("$.size").value(file.getSize()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long mediaId = objectMapper.readTree(response)
                .get("mediaId").asLong();

        assertTrue(mediaRepository.existsById(mediaId));
    }

    @Test
    void uploadMedia_withUnsupportedExtension_shouldReturnBadRequest()
            throws Exception {

        // Register user
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


        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                Base64.getDecoder().decode(
                        "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////2wBDAf//////////////////////////////////////////////////////////////////////////////////////wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAb/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIQAxAAAAH/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAEBAAEFAqf/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAEDAQE/AT//xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAECAQE/AT//2Q=="
                )
        );

        mockMvc.perform(multipart("/api/media")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadMedia_withMismatchedContent_shouldReturnBadRequest()
            throws Exception {

        // Register user
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

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "fake-image.jpg",
                "image/jpeg",
                "This is not an actual image".getBytes()
        );

        mockMvc.perform(multipart("/api/media")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadMedia_withEmptyFile_shouldReturnBadRequest()
            throws Exception {

        // Register user
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

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        mockMvc.perform(multipart("/api/media")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadMedia_withoutAuthentication_shouldReturnUnauthorized()
            throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                Base64.getDecoder().decode(
                        "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////2wBDAf//////////////////////////////////////////////////////////////////////////////////////wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAb/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIQAxAAAAH/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAEBAAEFAqf/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAEDAQE/AT//xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAECAQE/AT//2Q=="
                )
        );

        mockMvc.perform(multipart("/api/media")
                        .file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteMedia_byOwner_shouldReturnNoContent() throws Exception {

        // Register user
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

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                Base64.getDecoder().decode(
                        "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////2wBDAf//////////////////////////////////////////////////////////////////////////////////////wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAb/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIQAxAAAAH/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAEBAAEFAqf/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAEDAQE/AT//xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAECAQE/AT//2Q=="
                )
        );

        String response = mockMvc.perform(
                        multipart("/api/media")
                                .file(file)
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long mediaId = objectMapper.readTree(response)
                .get("mediaId").asLong();

        mockMvc.perform(delete("/api/media/" + mediaId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertFalse(mediaRepository.existsById(mediaId));
    }

    @Test
    void deleteMedia_byNonOwner_shouldReturnForbidden() throws Exception {

        // Register user
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

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                Base64.getDecoder().decode(
                        "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////2wBDAf//////////////////////////////////////////////////////////////////////////////////////wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAb/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIQAxAAAAH/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAEBAAEFAqf/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAEDAQE/AT//xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAECAQE/AT//2Q=="
                )
        );

        String response = mockMvc.perform(
                        multipart("/api/media")
                                .file(file)
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long mediaId = objectMapper.readTree(response)
                .get("mediaId").asLong();

        // Register user
        registerRequest = new CreateUserRequest();
        registerRequest.setUsername("user2");
        registerRequest.setEmail("user2@example.com");
        registerRequest.setPhone("9876543231");
        registerRequest.setPassword("Pass@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login
        loginRequest = new LoginRequest();
        loginRequest.setIdentifier("user2");
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

        mockMvc.perform(delete("/api/media/" + mediaId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());

        assertTrue(mediaRepository.existsById(mediaId));
    }

    @Test
    void deleteMedia_attachedToPost_shouldReturnConflict() throws Exception {

        // Register user
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

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                Base64.getDecoder().decode(
                        "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////2wBDAf//////////////////////////////////////////////////////////////////////////////////////wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAb/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIQAxAAAAH/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAEBAAEFAqf/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAEDAQE/AT//xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAECAQE/AT//2Q=="
                )
        );

        String response = mockMvc.perform(
                        multipart("/api/media")
                                .file(file)
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long mediaId = objectMapper.readTree(response)
                .get("mediaId").asLong();

        // Create post
        CreatePostRequest createPostRequest = new CreatePostRequest();

        createPostRequest.setText("Post with uploaded image");
        createPostRequest.setMediaIds(List.of(mediaId));

        String postResponse = mockMvc.perform(
                        post("/api/posts")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createPostRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long postId = objectMapper.readTree(postResponse)
                .get("postId").asLong();

        mockMvc.perform(delete("/api/media/" + mediaId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());

        assertTrue(mediaRepository.existsById(mediaId));
    }

    @Test
    void deleteMedia_withNonExistentId_shouldReturnNotFound()
            throws Exception {

        // Register user
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
        mockMvc.perform(delete("/api/media/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
