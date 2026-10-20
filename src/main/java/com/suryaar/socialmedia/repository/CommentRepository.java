
package com.suryaar.socialmedia.repository;

import com.suryaar.socialmedia.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository
        extends JpaRepository<Comment, Long> {

    Page<Comment> findByPostPostIdOrderByCreatedAtDesc(
            Long postId,
            Pageable pageable);
}
