
package com.suryaar.socialmedia.service;

import com.suryaar.socialmedia.dto.CreatePostRequest;
import com.suryaar.socialmedia.dto.PostResponse;
import com.suryaar.socialmedia.dto.PublicPostResponse;
import com.suryaar.socialmedia.dto.UpdatePostRequest;
import com.suryaar.socialmedia.entity.*;
import com.suryaar.socialmedia.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PostServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private FollowRepository followRepository;
    @Mock private PostRepository postRepository;
    @Mock private MediaRepository mediaRepository;
    @Mock private PostMediaRepository postMediaRepository;
    @Mock private LikeRepository likeRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private MediaStorageService mediaStorageService;

    private PostService postService;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        postService = new PostService(
                userRepository,
                followRepository,
                postRepository,
                mediaRepository,
                postMediaRepository,
                likeRepository,
                commentRepository,
                mediaStorageService
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }


    @Test
    void shouldRejectPostWithoutTextOrMedia() {
        CreatePostRequest request = new CreatePostRequest();
        request.setText("   ");
        request.setMediaIds(List.of());

        assertThrows(IllegalArgumentException.class,
                () -> postService.createPost(1L, request));

        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void shouldRejectPostWithTextExceeding250Characters() {
        CreatePostRequest request = new CreatePostRequest();
        request.setText("a".repeat(251));
        request.setMediaIds(List.of());

        assertThrows(IllegalArgumentException.class,
                () -> postService.createPost(1L, request));

        verifyNoInteractions(userRepository, mediaRepository);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void shouldRejectPostWithMoreThan10MediaItems() {
        CreatePostRequest request = new CreatePostRequest();
        request.setMediaIds(
                java.util.stream.LongStream.rangeClosed(1, 11)
                        .boxed().toList()
        );

        assertThrows(IllegalArgumentException.class,
                () -> postService.createPost(1L, request));

        verifyNoInteractions(userRepository, mediaRepository);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void shouldRejectDuplicateMediaIds() {
        CreatePostRequest request = new CreatePostRequest();
        request.setMediaIds(List.of(10L, 10L));

        assertThrows(IllegalArgumentException.class,
                () -> postService.createPost(1L, request));

        verifyNoInteractions(userRepository, mediaRepository);
        verify(postRepository, never()).save(any(Post.class));
    }


    @Test
    void shouldCreateTextPostSuccessfully() {
        // Arrange
        Long userId = 1L;

        User user = new User();
        user.setUserId(userId);
        user.setStatus(UserStatus.ACTIVE);

        CreatePostRequest request = new CreatePostRequest();
        request.setText("My first post");
        request.setMediaIds(List.of());

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(postRepository.save(any(Post.class)))
                .thenAnswer(invocation -> {
                    Post post = invocation.getArgument(0);
                    post.setPostId(100L);
                    return post;
                });

        // Act
        Post response = postService.createPost(userId, request);

        // Assert
        assertEquals(100L, response.getPostId());
        assertEquals("My first post", response.getText());
        assertEquals(user, response.getUser());

        verify(postRepository).save(any(Post.class));
        verifyNoInteractions(postMediaRepository);
    }

    @Test
    void shouldCreateMediaOnlyPostSuccessfully() {
        // Arrange
        Long userId = 1L;

        User user = new User();
        user.setUserId(userId);
        user.setStatus(UserStatus.ACTIVE);

        Media media = new Media();
        media.setMediaId(10L);
        media.setUploadedBy(user);
        media.setSize(1024L);

        CreatePostRequest request = new CreatePostRequest();
        request.setText(null);
        request.setMediaIds(List.of(10L));

        Post savedPost = new Post();
        savedPost.setPostId(100L);
        savedPost.setUser(user);
        savedPost.setText(null);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(mediaRepository.findByMediaIdIn(List.of(10L)))
                .thenReturn(List.of(media));

        when(postRepository.save(any(Post.class)))
                .thenAnswer(invocation -> {
                    Post post = invocation.getArgument(0);
                    post.setPostId(100L);
                    return post;
                });

        // Act
        Post response = postService.createPost(userId, request);

        // Assert
        assertEquals(100L, response.getPostId());
        assertEquals(null, response.getText());
        assertEquals(user, response.getUser());

        ArgumentCaptor<PostMedia> captor =
                ArgumentCaptor.forClass(PostMedia.class);

        verify(postMediaRepository).save(captor.capture());

        assertEquals(10L, captor.getValue().getMedia().getMediaId());
        assertEquals(1, captor.getValue().getPosition());
    }

    @Test
    void shouldCreatePostWithBothTextAndMediaSuccessfully() {
        // Arrange
        Long userId = 1L;

        User user = new User();
        user.setUserId(userId);
        user.setStatus(UserStatus.ACTIVE);

        Media media = new Media();
        media.setMediaId(10L);
        media.setUploadedBy(user);
        media.setSize(1024L);

        CreatePostRequest request = new CreatePostRequest();
        request.setText("My photo post");
        request.setMediaIds(List.of(10L));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(mediaRepository.findByMediaIdIn(List.of(10L)))
                .thenReturn(List.of(media));

        when(postRepository.save(any(Post.class)))
                .thenAnswer(invocation -> {
                    Post post = invocation.getArgument(0);
                    post.setPostId(100L);
                    return post;
                });

        // Act
        Post response = postService.createPost(userId, request);

        // Assert
        assertEquals(100L, response.getPostId());
        assertEquals("My photo post", response.getText());
        assertEquals(user, response.getUser());

        ArgumentCaptor<PostMedia> captor =
                ArgumentCaptor.forClass(PostMedia.class);

        verify(postMediaRepository).save(captor.capture());

        assertEquals(10L, captor.getValue().getMedia().getMediaId());
        assertEquals(1, captor.getValue().getPosition());
    }

    @Test
    void shouldRejectPostWhenMediaDoesNotExist() {
        Long userId = 1L;

        User user = new User();
        user.setUserId(userId);
        user.setStatus(UserStatus.ACTIVE);

        CreatePostRequest request = new CreatePostRequest();
        request.setMediaIds(List.of(10L));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(mediaRepository.findByMediaIdIn(List.of(10L)))
                .thenReturn(List.of());

        assertThrows(IllegalArgumentException.class,
                () -> postService.createPost(userId, request));

        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void shouldRejectMediaOwnedByAnotherUser() {
        Long userId = 1L;

        User user = new User();
        user.setUserId(userId);
        user.setStatus(UserStatus.ACTIVE);

        User anotherUser = new User();
        anotherUser.setUserId(2L);

        Media media = new Media();
        media.setMediaId(10L);
        media.setUploadedBy(anotherUser);
        media.setSize(1024L);

        CreatePostRequest request = new CreatePostRequest();
        request.setMediaIds(List.of(10L));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(mediaRepository.findByMediaIdIn(List.of(10L)))
                .thenReturn(List.of(media));

        assertThrows(ResponseStatusException.class,
                () -> postService.createPost(userId, request));

        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void shouldRejectMediaExceeding100MBTotal() {
        Long userId = 1L;

        User user = new User();
        user.setUserId(userId);
        user.setStatus(UserStatus.ACTIVE);

        Media media = new Media();
        media.setMediaId(10L);
        media.setUploadedBy(user);
        media.setSize(100L * 1024 * 1024 + 1);

        CreatePostRequest request = new CreatePostRequest();
        request.setMediaIds(List.of(10L));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(mediaRepository.findByMediaIdIn(List.of(10L)))
                .thenReturn(List.of(media));

        assertThrows(IllegalArgumentException.class,
                () -> postService.createPost(userId, request));

        verify(postRepository, never()).save(any(Post.class));
    }


    @Test
    void shouldAttachMediaInRequestedOrder() {
        Long userId = 1L;

        User user = new User();
        user.setUserId(userId);
        user.setStatus(UserStatus.ACTIVE);

        Media media10 = new Media();
        media10.setMediaId(10L);
        media10.setUploadedBy(user);
        media10.setSize(1024L);

        Media media20 = new Media();
        media20.setMediaId(20L);
        media20.setUploadedBy(user);
        media20.setSize(2048L);

        CreatePostRequest request = new CreatePostRequest();
        request.setText("Post with media");
        request.setMediaIds(List.of(20L, 10L));

        Post savedPost = new Post();
        savedPost.setPostId(100L);
        savedPost.setUser(user);
        savedPost.setText("Post with media");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(mediaRepository.findByMediaIdIn(List.of(20L, 10L)))
                .thenReturn(List.of(media10, media20));
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        postService.createPost(userId, request);

        ArgumentCaptor<PostMedia> captor =
                ArgumentCaptor.forClass(PostMedia.class);

        verify(postMediaRepository, times(2)).save(captor.capture());

        List<PostMedia> associations = captor.getAllValues();

        assertEquals(20L, associations.get(0).getMedia().getMediaId());
        assertEquals(1, associations.get(0).getPosition());

        assertEquals(10L, associations.get(1).getMedia().getMediaId());
        assertEquals(2, associations.get(1).getPosition());
    }


    @Test
    void shouldRejectUpdatingNonexistentPost() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        UpdatePostRequest request = new UpdatePostRequest();
        request.setText("Updated text");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> postService.updatePost(1L, 99L, request));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldRejectUpdatingAnotherUsersPost() {
        User owner = new User();
        owner.setUserId(2L);

        Post post = new Post();
        post.setPostId(10L);
        post.setUser(owner);

        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        UpdatePostRequest request = new UpdatePostRequest();
        request.setText("Updated text");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> postService.updatePost(1L, 10L, request));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void shouldRejectDeletingAnotherUsersPost() {
        User owner = new User();
        owner.setUserId(2L);

        Post post = new Post();
        post.setPostId(10L);
        post.setUser(owner);

        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> postService.deletePost(1L, 10L));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    void shouldRejectDeletingNonexistentPost() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> postService.deletePost(1L, 99L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verifyNoInteractions(commentRepository, likeRepository,
                postMediaRepository, mediaRepository, mediaStorageService);
    }

    @Test
    void shouldUpdateOwnPostSuccessfully() {
        Long userId = 1L;

        User user = new User();
        user.setUserId(userId);
        user.setUsername("surya_dev");

        Post post = new Post();
        post.setPostId(10L);
        post.setUser(user);
        post.setText("Old text");

        UpdatePostRequest request = new UpdatePostRequest();
        request.setText("Updated text");

        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(postMediaRepository.findByPostPostIdOrderByPositionAsc(10L))
                .thenReturn(List.of());
        when(postRepository.save(post)).thenReturn(post);

        PostResponse response = postService.updatePost(userId, 10L, request);

        assertEquals("Updated text", response.getText());
        verify(postRepository).save(post);
    }

    @Test
    void shouldDeleteOwnTextOnlyPostSuccessfully() {
        Long userId = 1L;

        User user = new User();
        user.setUserId(userId);

        Post post = new Post();
        post.setPostId(10L);
        post.setUser(user);

        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(postMediaRepository.findByPostPostId(10L)).thenReturn(List.of());

        postService.deletePost(userId, 10L);

        verify(commentRepository).deleteAllByPostId(10L);
        verify(likeRepository).deleteAllByPostId(10L);
        verify(postMediaRepository).deleteAll(List.of());
        verify(postMediaRepository).flush();
        verify(postRepository).delete(post);
        verify(postRepository).flush();
    }

    @Test
    void shouldDeleteMediaWhenNoOtherPostReferencesIt() throws Exception {
        Long userId = 1L;
        Long postId = 10L;
        Long mediaId = 20L;

        User user = new User();
        user.setUserId(userId);

        Post post = new Post();
        post.setPostId(postId);
        post.setUser(user);

        Media media = new Media();
        media.setMediaId(mediaId);
        media.setUrl("http://localhost/uploads/photo.jpg");

        PostMedia postMedia = new PostMedia(post, media, 1);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postMediaRepository.findByPostPostId(postId))
                .thenReturn(List.of(postMedia));
        when(postMediaRepository.existsByMediaMediaId(mediaId))
                .thenReturn(false);
        when(mediaRepository.findById(mediaId))
                .thenReturn(Optional.of(media));

        postService.deletePost(userId, postId);

        verify(mediaStorageService).deleteFile("photo.jpg");
        verify(mediaRepository).delete(media);
    }


    @Test
    void shouldPreserveMediaWhenAnotherPostReferencesIt() throws IOException {
        Long userId = 1L;
        Long postId = 10L;
        Long mediaId = 20L;

        User user = new User();
        user.setUserId(userId);

        Post post = new Post();
        post.setPostId(postId);
        post.setUser(user);

        Media media = new Media();
        media.setMediaId(mediaId);
        media.setUrl("http://localhost/uploads/shared.jpg");

        PostMedia postMedia = new PostMedia(post, media, 1);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postMediaRepository.findByPostPostId(postId))
                .thenReturn(List.of(postMedia));
        when(postMediaRepository.existsByMediaMediaId(mediaId))
                .thenReturn(true);

        postService.deletePost(userId, postId);

        verify(mediaStorageService, never()).deleteFile(anyString());
        verify(mediaRepository, never()).delete(any(Media.class));
    }


    @Test
    void shouldGetPublicPostForGuest() {
        User user = new User();
        user.setUserId(2L);
        user.setUsername("another_user");
        user.setProfilePicture("avatar.jpg");

        Post post = new Post();
        post.setPostId(10L);
        post.setUser(user);
        post.setText("Hello world");

        when(postRepository.findByPostIdAndUserStatus(10L, UserStatus.ACTIVE))
                .thenReturn(Optional.of(post));
        when(postMediaRepository.findByPostPostIdOrderByPositionAsc(10L))
                .thenReturn(List.of());
        when(likeRepository.countByPostPostId(10L)).thenReturn(5L);
        when(commentRepository.countByPostPostId(10L)).thenReturn(3L);

        PublicPostResponse response = postService.getPublicPost(10L, null);

        assertEquals(10L, response.getPostId());
        assertEquals("another_user", response.getUsername());
        assertEquals("Hello world", response.getText());
//        assertEquals(5L, response.getLikeCount());
//        assertEquals(3L, response.getCommentCount());
        assertNull(response.getLikedByMe());

        verify(likeRepository, never())
                .existsByUserUserIdAndPostPostId(anyLong(), anyLong());
    }

    @Test
    void shouldGetPublicPostWithViewerLikeStatus() {
        User user = new User();
        user.setUserId(2L);
        user.setUsername("another_user");

        Post post = new Post();
        post.setPostId(10L);
        post.setUser(user);
        post.setText("Hello world");


        when(postRepository.findByPostIdAndUserStatus(10L, UserStatus.ACTIVE))
                .thenReturn(Optional.of(post));
        when(postMediaRepository.findByPostPostIdOrderByPositionAsc(10L))
                .thenReturn(List.of());
        when(likeRepository.countByPostPostId(10L)).thenReturn(5L);
        when(commentRepository.countByPostPostId(10L)).thenReturn(3L);
        when(likeRepository.existsByUserUserIdAndPostPostId(1L, 10L))
                .thenReturn(true);

        PublicPostResponse response = postService.getPublicPost(10L, 1L);

        assertEquals(true, response.getLikedByMe());
    }


    @Test
    void shouldRejectInvalidPublicPostPagination() {
        assertThrows(ResponseStatusException.class,
                () -> postService.getPublicPosts("surya_dev", -1, 10));

        assertThrows(ResponseStatusException.class,
                () -> postService.getPublicPosts("surya_dev", 0, 0));

        assertThrows(ResponseStatusException.class,
                () -> postService.getPublicPosts("surya_dev", 0, 51));
    }

    @Test
    void shouldRejectInvalidFeedPagination() {
        assertThrows(ResponseStatusException.class,
                () -> postService.getFeed(1L, -1, 10));

        assertThrows(ResponseStatusException.class,
                () -> postService.getFeed(1L, 0, 0));

        assertThrows(ResponseStatusException.class,
                () -> postService.getFeed(1L, 0, 51));
    }


    @Test
    void shouldReturnEmptyFeedWhenUserFollowsNobody() {
        Long userId = 1L;

        User user = new User();
        user.setUserId(userId);
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(followRepository.findByFollower_UserIdOrderByFollowingUsernameAsc(
                eq(userId), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<PublicPostResponse> result =
                postService.getFeed(userId, 0, 10);

        assertTrue(result.isEmpty());

        verify(postRepository, never())
                .findByUserUserIdInAndUserStatusAndUserUserIdNotOrderByCreatedAtDesc(
                        anyList(), any(UserStatus.class), anyLong(),
                        any(Pageable.class));
    }

    @Test
    void shouldRejectFeedForMissingUser() {
        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> postService.getFeed(99L, 0, 10));

        verifyNoInteractions(followRepository);
    }

    @Test
    void shouldReturnPublicPostsForExistingUser() {
        String username = "surya";
        User user = new User();
        user.setUserId(1L);
        user.setUsername(username);
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByUsernameAndStatus(
                username, UserStatus.ACTIVE))
                .thenReturn(Optional.of(user));

        when(postRepository
                .findByUserUsernameAndUserStatusOrderByCreatedAtDesc(
                        eq(username),
                        eq(UserStatus.ACTIVE),
                        any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<PublicPostResponse> result =
                postService.getPublicPosts(username, 0, 10);

        assertTrue(result.isEmpty());

        verify(postRepository)
                .findByUserUsernameAndUserStatusOrderByCreatedAtDesc(
                        eq(username),
                        eq(UserStatus.ACTIVE),
                        any(Pageable.class));
    }

    @Test
    void shouldRejectPublicPostsForMissingUser() {
        when(userRepository.findByUsernameAndStatus(
                "unknown", UserStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> postService.getPublicPosts("unknown", 0, 10));

        verifyNoInteractions(postRepository);
    }

    @Test
    void shouldRejectUpdateWhenPostWouldBeEmpty() {
        Long userId = 1L;
        Long postId = 10L;

        User user = new User();
        user.setUserId(userId);

        Post post = new Post();
        post.setPostId(postId);
        post.setUser(user);

        when(postRepository.findById(postId))
                .thenReturn(Optional.of(post));
        when(postMediaRepository.findByPostPostId(postId))
                .thenReturn(List.of());

        UpdatePostRequest request = new UpdatePostRequest();
        request.setText("   ");

        assertThrows(
                ResponseStatusException.class,
                () -> postService.updatePost(userId, postId, request));

        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void shouldRejectCreatePostForInactiveUser() {
        Long userId = 1L;

        User user = new User();
        user.setUserId(userId);
        user.setStatus(UserStatus.DELETED);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        CreatePostRequest request = new CreatePostRequest();
        request.setText("Hello world");

        assertThrows(
                IllegalArgumentException.class,
                () -> postService.createPost(userId, request));

        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void shouldReturnFeedWhenUserFollowsSomeone() {
        Long userId = 1L;
        Long followingId = 2L;

        User currentUser = new User();
        currentUser.setUserId(userId);
        currentUser.setStatus(UserStatus.ACTIVE);

        User followedUser = new User();
        followedUser.setUserId(followingId);
        followedUser.setUsername("followedUser");

        Follow follow = new Follow(currentUser, followedUser);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(currentUser));

        when(followRepository
                .findByFollower_UserIdOrderByFollowingUsernameAsc(
                        eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(follow)));

        when(postRepository
                .findByUserUserIdInAndUserStatusAndUserUserIdNotOrderByCreatedAtDesc(
                        eq(List.of(followingId)),
                        eq(UserStatus.ACTIVE),
                        eq(userId),
                        any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<PublicPostResponse> result =
                postService.getFeed(userId, 0, 10);

        assertTrue(result.isEmpty());

        verify(postRepository)
                .findByUserUserIdInAndUserStatusAndUserUserIdNotOrderByCreatedAtDesc(
                        eq(List.of(followingId)),
                        eq(UserStatus.ACTIVE),
                        eq(userId),
                        any(Pageable.class));
    }
}
