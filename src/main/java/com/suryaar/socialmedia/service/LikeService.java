package com.suryaar.socialmedia.service;

import com.suryaar.socialmedia.entity.Like;
import com.suryaar.socialmedia.entity.Post;
import com.suryaar.socialmedia.entity.User;
import com.suryaar.socialmedia.entity.UserStatus;
import com.suryaar.socialmedia.repository.LikeRepository;
import com.suryaar.socialmedia.repository.PostRepository;
import com.suryaar.socialmedia.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LikeService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;

    public LikeService(UserRepository userRepository, PostRepository postRepository, LikeRepository likeRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.likeRepository = likeRepository;
    }

    @Transactional
    public void likePost(Long userId, Long postId) {

        User user = userRepository.findById(userId)
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));

        Post post = postRepository
                .findByPostIdAndUserStatus(postId, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Post not found"
                ));

        boolean alreadyLiked =
                likeRepository.existsByUserUserIdAndPostPostId(
                        userId, postId
                );

        if (alreadyLiked) {
            return;
        }

        Like like = new Like(user, post);
        likeRepository.save(like);
    }


    @Transactional
    public void unlikePost(Long userId, Long postId) {

        userRepository.findById(userId)
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));

        postRepository.findByPostIdAndUserStatus(
                postId, UserStatus.ACTIVE
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Post not found"
        ));

        likeRepository.deleteLike(userId, postId);
    }
}
