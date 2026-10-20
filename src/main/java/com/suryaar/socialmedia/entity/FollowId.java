
package com.suryaar.socialmedia.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FollowId implements Serializable {

    @Column(name = "follower_id")
    private Long followerId;

    @Column(name = "following_id")
    private Long followingId;

    protected FollowId() {
    }

    public FollowId(Long followerId, Long followingId) {
        this.followerId = followerId;
        this.followingId = followingId;
    }

    public Long getFollowerId() {
        return followerId;
    }

    public Long getFollowingId() {
        return followingId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof FollowId other)) {
            return false;
        }

        return Objects.equals(followerId, other.followerId)
                && Objects.equals(followingId, other.followingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(followerId, followingId);
    }
}
