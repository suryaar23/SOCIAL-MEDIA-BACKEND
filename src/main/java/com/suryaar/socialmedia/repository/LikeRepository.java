
package com.suryaar.socialmedia.repository;

import com.suryaar.socialmedia.entity.Like;
import com.suryaar.socialmedia.entity.LikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LikeRepository
        extends JpaRepository<Like, LikeId> {

    boolean existsByUserUserIdAndPostPostId(
            Long userId,
            Long postId);

    long countByPostPostId(Long postId);
}
