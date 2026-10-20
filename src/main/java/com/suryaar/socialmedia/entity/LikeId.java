
package com.suryaar.socialmedia.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class LikeId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "post_id")
    private Long postId;

    protected LikeId() {
    }

    public LikeId(Long userId, Long postId) {
        this.userId = userId;
        this.postId = postId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getPostId() {
        return postId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof LikeId other)) {
            return false;
        }

        return Objects.equals(userId, other.userId)
                && Objects.equals(postId, other.postId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, postId);
    }
}
