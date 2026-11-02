package com.suryaar.socialmedia.service;

import com.suryaar.socialmedia.dto.AdminPostResponse;
import com.suryaar.socialmedia.dto.AdminUserResponse;
import com.suryaar.socialmedia.dto.MediaResponse;
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
public class AdminService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostMediaRepository postMediaRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final MediaStorageService mediaStorageService;

    public AdminService(UserRepository userRepository, PostRepository postRepository, PostMediaRepository postMediaRepository, LikeRepository likeRepository, CommentRepository commentRepository, MediaStorageService mediaStorageService) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.postMediaRepository = postMediaRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.mediaStorageService = mediaStorageService;
    }

    @Transactional(readOnly = true)
    public Page<AdminUserResponse> getAdminUsers(int page, int size) {

        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid pagination parameters"
            );
        }

        Pageable pageable = PageRequest.of(
                page, size, Sort.by("createdAt").descending()
        );

        return userRepository
                .findAllByOrderByCreatedAtDesc(pageable)
                .map(user -> new AdminUserResponse(
                        user.getUserId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getStatus(),
                        user.getCreatedAt()
                ));
    }


    @Transactional(readOnly = true)
    public Page<AdminPostResponse> getAdminPosts(int page, int size) {

        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid pagination parameters"
            );
        }

        Pageable pageable = PageRequest.of(
                page, size, Sort.by("createdAt").descending()
        );

        return postRepository
                .findByUserStatusOrderByCreatedAtDesc(
                        UserStatus.ACTIVE, pageable
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
                            likeRepository.countByPostPostId(
                                    post.getPostId()
                            );

                    long commentCount =
                            commentRepository.countByPostPostId(
                                    post.getPostId()
                            );

                    return new AdminPostResponse(
                            post.getPostId(),
                            post.getUser().getUsername(),
                            post.getUser().getProfilePicture(),
                            post.getText(),
                            mediaResponses,
                            likeCount,
                            commentCount,
                            post.getCreatedAt(),
                            post.getUpdatedAt()
                    );
                });
    }
}
