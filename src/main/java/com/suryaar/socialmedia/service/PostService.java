package com.suryaar.socialmedia.service;

import com.suryaar.socialmedia.dto.*;
import com.suryaar.socialmedia.entity.Post;
import com.suryaar.socialmedia.entity.User;
import com.suryaar.socialmedia.entity.UserStatus;
import com.suryaar.socialmedia.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.suryaar.socialmedia.entity.Media;
import com.suryaar.socialmedia.entity.PostMedia;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PostService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final PostRepository postRepository;
    private final MediaRepository mediaRepository;
    private final PostMediaRepository postMediaRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final MediaStorageService mediaStorageService;

    public PostService(UserRepository userRepository, FollowRepository followRepository, PostRepository postRepository, MediaRepository mediaRepository, PostMediaRepository postMediaRepository, LikeRepository likeRepository, CommentRepository commentRepository, MediaStorageService mediaStorageService) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.postRepository = postRepository;
        this.mediaRepository = mediaRepository;
        this.postMediaRepository = postMediaRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.mediaStorageService = mediaStorageService;
    }


    @Transactional(readOnly = true)
    public Page<PublicPostResponse> getPublicPosts(
            String username, int page, int size) {

        if (page < 0 || size < 1 || size > 50) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid pagination parameters"
            );
        }

        User user = userRepository
                .findByUsernameAndStatus(username, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));

        Pageable pageable = PageRequest.of(
                page, size, Sort.by("createdAt").descending()
        );


        return postRepository
                .findByUserUsernameAndUserStatusOrderByCreatedAtDesc(
                        user.getUsername(), UserStatus.ACTIVE, pageable
                )
                .map(post -> {
                    List<MediaResponse> mediaResponses =
                            postMediaRepository
                                    .findByPostPostIdOrderByPositionAsc(
                                            post.getPostId()
                                    )
                                    .stream()
                                    .map(pm -> {
                                        var media = pm.getMedia();

                                        return new MediaResponse(
                                                media.getMediaId(),
                                                media.getType(),
                                                mediaStorageService.generatePresignedUrl(media.getUrl()),
                                                media.getName(),
                                                media.getSize()
                                        );
                                    })
                                    .toList();

                    long likeCount =
                            likeRepository.countByPostPostId(post.getPostId());

                    long commentCount =
                            commentRepository.countByPostPostId(post.getPostId());

                    return new PublicPostResponse(
                            post.getPostId(),
                            post.getUser().getUsername(),
                            post.getUser().getProfilePicture(),
                            post.getText(),
                            mediaResponses,
                            likeCount,
                            commentCount,
                            post.getCreatedAt(),
                            post.getUpdatedAt(),
                            null
                    );
                });
    }

    @Transactional(readOnly = true)
    public PublicPostResponse getPublicPost(Long postId, Long viewerUserId) {

        Post post = postRepository
                .findByPostIdAndUserStatus(postId, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Post not found"
                ));

        List<MediaResponse> mediaResponses =
                postMediaRepository
                        .findByPostPostIdOrderByPositionAsc(post.getPostId())
                        .stream()
                        .map(pm -> {
                            var media = pm.getMedia();

                            return new MediaResponse(
                                    media.getMediaId(),
                                    media.getType(),
                                    mediaStorageService.generatePresignedUrl(media.getUrl()),
                                    media.getName(),
                                    media.getSize()
                            );
                        })
                        .toList();

        long likeCount = likeRepository.countByPostPostId(postId);
        long commentCount = commentRepository.countByPostPostId(postId);

        Boolean likedByMe = viewerUserId == null
                ? null
                : likeRepository.existsByUserUserIdAndPostPostId(
                viewerUserId, postId
        );

        return new PublicPostResponse(
                post.getPostId(),
                post.getUser().getUsername(),
                post.getUser().getProfilePicture(),
                post.getText(),
                mediaResponses,
                likeCount,
                commentCount,
                post.getCreatedAt(),
                post.getUpdatedAt(),
                likedByMe
        );
    }

    @Transactional(readOnly = true)
    public Page<PublicPostResponse> getFeed(
            Long userId, int page, int size) {

        if (page < 0 || size < 1 || size > 50) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid pagination parameters"
            );
        }

        User currentUser = userRepository.findById(userId)
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));

        List<Long> followingIds = followRepository
                .findByFollower_UserIdOrderByFollowingUsernameAsc(
                        userId, Pageable.unpaged()
                )
                .stream()
                .map(follow -> follow.getFollowing().getUserId())
                .toList();

        if (followingIds.isEmpty()) {
            return Page.empty(PageRequest.of(page, size));
        }

        Pageable pageable = PageRequest.of(
                page, size, Sort.by("createdAt").descending()
        );

        return postRepository
                .findByUserUserIdInAndUserStatusAndUserUserIdNotOrderByCreatedAtDesc(
                        followingIds, UserStatus.ACTIVE,
                        currentUser.getUserId(), pageable
                )
                .map(post -> {
                    List<MediaResponse> mediaResponses =
                            postMediaRepository
                                    .findByPostPostIdOrderByPositionAsc(
                                            post.getPostId()
                                    )
                                    .stream()
                                    .map(pm -> {
                                        var media = pm.getMedia();
                                        return new MediaResponse(
                                                media.getMediaId(),
                                                media.getType(),
                                                mediaStorageService.generatePresignedUrl(media.getUrl()),
                                                media.getName(),
                                                media.getSize()
                                        );
                                    })
                                    .toList();

                    long likeCount =
                            likeRepository.countByPostPostId(post.getPostId());

                    long commentCount =
                            commentRepository.countByPostPostId(post.getPostId());

                    boolean likedByMe =
                            likeRepository.existsByUserUserIdAndPostPostId(
                                    userId, post.getPostId()
                            );

                    return new PublicPostResponse(
                            post.getPostId(),
                            post.getUser().getUsername(),
                            post.getUser().getProfilePicture(),
                            post.getText(),
                            mediaResponses,
                            likeCount,
                            commentCount,
                            post.getCreatedAt(),
                            post.getUpdatedAt(),
                            likedByMe
                    );
                });
    }

    @Transactional
    public PostResponse createPost(Long userId, CreatePostRequest request) {

        String text = request.getText();

        List<Long> mediaIds = request.getMediaIds() == null
                ? List.of()
                : request.getMediaIds();

        // Require text or media.
        boolean hasText = text != null && !text.isBlank();

        if (!hasText && mediaIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "A post must contain text or at least one media item"
            );
        }

        // Validate text length.
        if (text != null && text.length() > 250) {
            throw new IllegalArgumentException(
                    "Post text cannot exceed 250 characters"
            );
        }

        // Limit the number of media items.
        if (mediaIds.size() > 10) {
            throw new IllegalArgumentException(
                    "A post cannot contain more than 10 media items"
            );
        }

        // Prevent duplicate media IDs.
        Set<Long> uniqueMediaIds = new HashSet<>(mediaIds);

        if (uniqueMediaIds.size() != mediaIds.size()) {
            throw new IllegalArgumentException(
                    "Duplicate media IDs are not allowed"
            );
        }

        // Verify the author exists and is active.
        User user = userRepository.findById(userId)
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Active user not found"
                ));

        // Load and verify all media records.
        List<Media> mediaList = mediaIds.isEmpty()
                ? List.of()
                : mediaRepository.findByMediaIdIn(mediaIds);

        // Verify all media records exist.
        if (mediaList.size() != mediaIds.size()) {
            throw new IllegalArgumentException(
                    "One or more media IDs do not exist"
            );
        }

        //Ownership check
        boolean allMediaOwnedByUser = mediaList.stream()
                .allMatch(media ->
                        media.getUploadedBy() != null
                                && media.getUploadedBy()
                                .getUserId()
                                .equals(userId)
                );

        if (!allMediaOwnedByUser) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot attach media uploaded by another user"
            );
        }

        // Enforce the 100 MB total limit.
        long totalSize = mediaList.stream()
                .mapToLong(Media::getSize)
                .sum();

        long maxTotalSize = 100L * 1024 * 1024;

        if (totalSize > maxTotalSize) {
            throw new IllegalArgumentException(
                    "Total media size cannot exceed 100 MB"
            );
        }

        // Create the post.
        Post post = new Post();
        post.setUser(user);
        post.setText(text);

        Post savedPost = postRepository.save(post);

        // Attach media in the order supplied by the client.
        for (int i = 0; i < mediaIds.size(); i++) {

            Long mediaId = mediaIds.get(i);

            Media media = mediaList.stream()
                    .filter(m -> m.getMediaId().equals(mediaId))
                    .findFirst()
                    .orElseThrow();

            PostMedia postMedia = new PostMedia(
                    savedPost,
                    media,
                    i + 1
            );

            postMediaRepository.save(postMedia);
        }

        return new PostResponse(
                savedPost.getPostId(),
                user.getUserId(),
                user.getUsername(),
                savedPost.getText(),
                savedPost.getCreatedAt(),
                savedPost.getUpdatedAt()
        );
    }

    @Transactional
    public PostResponse updatePost(
            Long userId,
            Long postId,
            UpdatePostRequest request
    ) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Post not found"
                ));

        if (!post.getUser().getUserId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot update another user's post"
            );
        }

        String text = request.getText();

        boolean hasText = text != null && !text.isBlank();

        boolean hasMedia = !postMediaRepository
                .findByPostPostIdOrderByPositionAsc(postId)
                .isEmpty();

        if (!hasText && !hasMedia) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A post must contain text or at least one media item"
            );
        }

        post.setText(text);

        postRepository.save(post);

        return new PostResponse(
                post.getPostId(),
                post.getUser().getUserId(),
                post.getUser().getUsername(),
                post.getText(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }


    @Transactional
    public void deletePost(Long userId, Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Post not found"
                ));

        if (!post.getUser().getUserId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot delete another user's post"
            );
        }

        List<PostMedia> postMediaList = postMediaRepository.findByPostPostId(postId);

        // Get the IDs of the media items.
        List<Long> mediaIds = postMediaList.stream()
                .map(postMedia -> postMedia.getMedia().getMediaId())
                .distinct()
                .toList();


        // Delete comments and likes associated with this post.
        commentRepository.deleteAllByPostId(postId);
        likeRepository.deleteAllByPostId(postId);

        // Explicitly remove the media associations.
        postMediaRepository.deleteAll(postMediaList);
        postMediaRepository.flush();

        // Now delete the post.
        postRepository.delete(post);
        postRepository.flush();

        // Delete only media that no other post references.
        for (Long mediaId : mediaIds) {

            if (!postMediaRepository.existsByMediaMediaId(mediaId)) {

                Media media = mediaRepository.findById(mediaId)
                        .orElse(null);

                if (media != null) {
                    String storedFilename = media.getUrl();

                    mediaStorageService.deleteFile(storedFilename);

                    mediaRepository.delete(media);
                }
            }
        }
    }
}
