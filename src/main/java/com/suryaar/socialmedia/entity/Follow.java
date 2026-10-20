package com.suryaar.socialmedia.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "follows", indexes = {@Index(name = "idx_follows_following", columnList = "following_id")}, uniqueConstraints = {@UniqueConstraint(name = "uk_followers_following", columnNames = {"follower_id", "following_id"})})
public class Follow {

    @EmbeddedId
    private FollowId id;

    @MapsId("followerId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    @MapsId("followingId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "following_id", nullable = false)
    private User following;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Follow() {
    }

    public Follow(User follower, User following) {
        this.follower = follower;
        this.following = following;
        this.id = new FollowId(follower.getUserId(), following.getUserId());
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public FollowId getId() {
        return id;
    }

    public User getFollower() {
        return follower;
    }

    public User getFollowing() {
        return following;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
