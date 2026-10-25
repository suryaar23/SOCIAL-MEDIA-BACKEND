
package com.suryaar.socialmedia.repository;

import com.suryaar.socialmedia.entity.Post;
import com.suryaar.socialmedia.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;

public interface PostRepository
        extends JpaRepository<Post, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM Post p WHERE p.user.userId = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);

    Page<Post> findByUserUsernameAndUserStatusOrderByCreatedAtDesc(
            String username,
            UserStatus status,
            Pageable pageable
    );

    Optional<Post> findByPostIdAndUserStatus(
            Long postId,
            UserStatus status
    );

    Page<Post> findByUserUserIdInAndUserStatusAndUserUserIdNotOrderByCreatedAtDesc(
            Collection<Long> userIds,
            UserStatus status,
            Long excludedUserId,
            Pageable pageable
    );
}
