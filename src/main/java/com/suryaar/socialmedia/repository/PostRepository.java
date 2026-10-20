
package com.suryaar.socialmedia.repository;

import com.suryaar.socialmedia.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PostRepository
        extends JpaRepository<Post, Long> {

    Page<Post> findByUserUserIdOrderByCreatedAtDesc(
            Long userId,
            Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM Post p WHERE p.user.userId = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);
}
