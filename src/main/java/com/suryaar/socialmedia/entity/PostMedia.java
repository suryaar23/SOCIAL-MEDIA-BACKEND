
package com.suryaar.socialmedia.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "post_media",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_post_media_position",
                        columnNames = {"post_id", "position"}
                )
        }
)
public class PostMedia {

    @EmbeddedId
    private PostMediaId id;

    @MapsId("postId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @MapsId("mediaId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "media_id", nullable = false)
    private Media media;

    @Column(name = "position", nullable = false)
    private Integer position;

    protected PostMedia() {
    }

    public PostMedia(Post post, Media media, Integer position) {
        this.post = post;
        this.media = media;
        this.position = position;
        this.id = new PostMediaId(
                post.getPostId(),
                media.getMediaId()
        );
    }

    public PostMediaId getId() {
        return id;
    }

    public Post getPost() {
        return post;
    }

    public Media getMedia() {
        return media;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }
}
