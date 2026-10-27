
package com.suryaar.socialmedia.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "media")
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "media_id")
    private Long mediaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private MediaType type;

    @Column(name = "url", nullable = false, length = 255)
    private String url;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "size", nullable = false)
    private Long size;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    protected Media() {
    }

    public Media(MediaType type, String url, String name, Long size, User uploadedBy) {
        this.type = type;
        this.url = url;
        this.name = name;
        this.size = size;
        this.uploadedBy = uploadedBy;
    }

    public Long getMediaId() {
        return mediaId;
    }

    public MediaType getType() {
        return type;
    }

    public void setType(MediaType type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public User getUploadedBy() { return uploadedBy; }

    public void setUploadedBy(User uploadedBy) { this.uploadedBy = uploadedBy; }
}
