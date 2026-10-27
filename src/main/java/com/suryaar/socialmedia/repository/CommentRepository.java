
package com.suryaar.socialmedia.repository;

import com.suryaar.socialmedia.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommentRepository
        extends JpaRepository<Comment, Long> {

    Page<Comment> findByPostPostIdOrderByCreatedAtDesc(
            Long postId,
            Pageable pageable);

    long countByPostPostId(Long postId);

    Optional<Comment> findByCommentIdAndPostPostId(
            Long commentId,
            Long postId
    );

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.post.postId = :postId")
    int deleteAllByPostId(@Param("postId") Long postId);
}
