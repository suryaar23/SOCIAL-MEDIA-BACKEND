
package com.suryaar.socialmedia.repository;

import com.suryaar.socialmedia.entity.Follow;
import com.suryaar.socialmedia.entity.FollowId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface FollowRepository
        extends JpaRepository<Follow, FollowId> {

    @Modifying
    @Transactional
    @Query("""
        DELETE FROM Follow f
        WHERE f.follower.userId = :userId
           OR f.following.userId = :userId
        """)
    int deleteAllRelationshipsForUser(
            @Param("userId") Long userId);

    long countByFollowing_UserId(Long userId);

    long countByFollower_UserId(Long userId);

    boolean existsByFollower_UserIdAndFollowing_UserId(
            Long followerId,
            Long followingId);

}
