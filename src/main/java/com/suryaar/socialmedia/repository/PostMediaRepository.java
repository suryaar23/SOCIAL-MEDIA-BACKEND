
package com.suryaar.socialmedia.repository;

import com.suryaar.socialmedia.entity.PostMedia;
import com.suryaar.socialmedia.entity.PostMediaId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostMediaRepository
        extends JpaRepository<PostMedia, PostMediaId> {

    List<PostMedia> findByPostPostIdOrderByPositionAsc(
            Long postId);

    boolean existsByMediaMediaId(Long mediaId);

    List<PostMedia> findByPostPostId(Long postId);
}
