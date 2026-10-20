
package com.suryaar.socialmedia.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "likes",
        indexes = {
                @Index(
                        name = "idx_likes_post",
                        columnList = "post_id"
                )
        }
)
public class Like {

    @EmbeddedId
    private LikeId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @MapsId("postId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Like() {
    }

    public Like(User user, Post post) {
        this.user = user;
        this.post = post;
        this.id = new LikeId(
                user.getUserId(),
                post.getPostId()
        );
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public LikeId getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Post getPost() {
        return post;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
