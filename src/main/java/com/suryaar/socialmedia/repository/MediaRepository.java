
package com.suryaar.socialmedia.repository;

import com.suryaar.socialmedia.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface MediaRepository
        extends JpaRepository<Media, Long> {

    List<Media> findByMediaIdIn(Collection<Long> mediaIds);
}
