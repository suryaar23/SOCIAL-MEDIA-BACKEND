package com.suryaar.socialmedia.service;

import com.suryaar.socialmedia.dto.CommentResponse;
import com.suryaar.socialmedia.dto.CreateCommentRequest;
import com.suryaar.socialmedia.dto.UpdateCommentRequest;
import com.suryaar.socialmedia.entity.Comment;
import com.suryaar.socialmedia.entity.Post;
import com.suryaar.socialmedia.entity.User;
import com.suryaar.socialmedia.entity.UserStatus;
import com.suryaar.socialmedia.repository.CommentRepository;
import com.suryaar.socialmedia.repository.PostRepository;
import com.suryaar.socialmedia.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CommentService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public CommentService(UserRepository userRepository, PostRepository postRepository, CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }


    @Transactional
    public CommentResponse createComment(
            Long userId,
            Long postId,
            CreateCommentRequest request) {

        User user = userRepository
                .findById(userId)
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));

        Post post = postRepository
                .findByPostIdAndUserStatus(postId, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Post not found"
                ));

        Comment comment = new Comment(
                user,
                post,
                request.getText().trim()
        );

        Comment savedComment = commentRepository.save(comment);

        return new CommentResponse(
                savedComment.getCommentId(),
                user.getUsername(),
                user.getProfilePicture(),
                savedComment.getText(),
                savedComment.getCreatedAt(),
                savedComment.getUpdatedAt()
        );
    }


    @Transactional(readOnly = true)
    public Page<CommentResponse> getComments(
            Long userId, Long postId, int page, int size) {

        if (page < 0 || size < 1 || size > 50) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid pagination parameters"
            );
        }

        // Ensure the requesting user is active.
        userRepository.findById(userId)
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));

        // Ensure the post exists and its author is active.
        postRepository.findByPostIdAndUserStatus(
                postId, UserStatus.ACTIVE
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Post not found"
        ));

        Pageable pageable = PageRequest.of(
                page, size, Sort.by("createdAt").descending()
        );

        return commentRepository
                .findByPostPostIdOrderByCreatedAtDesc(postId, pageable)
                .map(comment -> {
                    User author = comment.getUser();

                    boolean deletedAuthor =
                            author.getStatus() == UserStatus.DELETED;

                    return new CommentResponse(
                            comment.getCommentId(),
                            deletedAuthor ? "Deleted User" : author.getUsername(),
                            deletedAuthor ? null : author.getProfilePicture(),
                            comment.getText(),
                            comment.getCreatedAt(),
                            comment.getUpdatedAt()
                    );
                });
    }


    @Transactional
    public CommentResponse updateComment(
            Long userId,
            Long postId,
            Long commentId,
            UpdateCommentRequest request) {

        User user = userRepository.findById(userId)
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));

        Comment comment = commentRepository
                .findByCommentIdAndPostPostId(commentId, postId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Comment not found"
                ));

        if (!comment.getUser().getUserId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only edit your own comments"
            );
        }

        comment.setText(request.getText().trim());

        return new CommentResponse(
                comment.getCommentId(),
                user.getUsername(),
                user.getProfilePicture(),
                comment.getText(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }


    @Transactional
    public void deleteComment(
            Long userId,
            Long postId,
            Long commentId) {

        userRepository.findById(userId)
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));

        Comment comment = commentRepository
                .findByCommentIdAndPostPostId(commentId, postId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Comment not found"
                ));

        if (!comment.getUser().getUserId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only delete your own comments"
            );
        }

        commentRepository.delete(comment);
    }
}
