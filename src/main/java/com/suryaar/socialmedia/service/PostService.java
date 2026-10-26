package com.suryaar.socialmedia.service;

import com.suryaar.socialmedia.dto.MediaResponse;
import com.suryaar.socialmedia.dto.PublicPostResponse;
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

import java.util.List;

@Service
public class PostService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final PostRepository postRepository;
    private final PostMediaRepository postMediaRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    public PostService(UserRepository userRepository, FollowRepository followRepository, PostRepository postRepository, PostMediaRepository postMediaRepository, LikeRepository likeRepository, CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.postRepository = postRepository;
        this.postMediaRepository = postMediaRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
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
                                                media.getUrl(),
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
                                    media.getUrl(),
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
                                                media.getUrl(),
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
}
