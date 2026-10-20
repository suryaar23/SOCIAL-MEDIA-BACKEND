CREATE DATABASE social_media;

USE social_media;

-- =====================================================
-- 1. USERS
-- =====================================================

CREATE TABLE users (
    user_id BIGINT NOT NULL AUTO_INCREMENT,

    username VARCHAR(10)
        CHARACTER SET utf8mb4
        COLLATE utf8mb4_0900_as_cs NULL,

    email VARCHAR(255)
        CHARACTER SET utf8mb4
        COLLATE utf8mb4_0900_ai_ci NULL,

    phone VARCHAR(20) NULL,
    password VARCHAR(255) NULL,
    profile_picture VARCHAR(255) NULL,

    status ENUM('ACTIVE', 'DELETED')
        NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_users
        PRIMARY KEY (user_id),

    CONSTRAINT uq_users_username
        UNIQUE (username),

    CONSTRAINT uq_users_email
        UNIQUE (email),

    CONSTRAINT uq_users_phone
        UNIQUE (phone),

    CONSTRAINT chk_users_username_length
        CHECK (
            username IS NULL
            OR CHAR_LENGTH(username) BETWEEN 5 AND 10
        ),

    CONSTRAINT chk_users_status_fields
        CHECK (
            (
                status = 'ACTIVE'
                AND username IS NOT NULL
                AND email IS NOT NULL
                AND phone IS NOT NULL
                AND password IS NOT NULL
            )
            OR
            (
                status = 'DELETED'
                AND username IS NULL
                AND email IS NULL
                AND phone IS NULL
                AND password IS NULL
                AND profile_picture IS NULL
            )
        )
) ENGINE = InnoDB;


-- =====================================================
-- 2. POSTS
-- =====================================================

CREATE TABLE posts (
    post_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    text VARCHAR(250) NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_posts
        PRIMARY KEY (post_id),

    INDEX idx_posts_user_created (user_id, created_at),

    CONSTRAINT fk_posts_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
) ENGINE = InnoDB;


-- =====================================================
-- 3. MEDIA
-- =====================================================

CREATE TABLE media (
    media_id BIGINT NOT NULL AUTO_INCREMENT,

    type ENUM('IMAGE', 'VIDEO', 'AUDIO') NOT NULL,
    url VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    size BIGINT NOT NULL,

    CONSTRAINT pk_media
        PRIMARY KEY (media_id),

    CONSTRAINT chk_media_size
        CHECK (size >= 0)
) ENGINE = InnoDB;


-- =====================================================
-- 4. POST_MEDIA
-- =====================================================

CREATE TABLE post_media (
    post_id BIGINT NOT NULL,
    media_id BIGINT NOT NULL,
    position INT NOT NULL,

    CONSTRAINT pk_post_media
        PRIMARY KEY (post_id, media_id),

    CONSTRAINT uq_post_media_position
        UNIQUE (post_id, position),

    CONSTRAINT chk_post_media_position
        CHECK (position >= 1),

    CONSTRAINT fk_post_media_post
        FOREIGN KEY (post_id)
        REFERENCES posts(post_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_post_media_media
        FOREIGN KEY (media_id)
        REFERENCES media(media_id)
        ON DELETE CASCADE
) ENGINE = InnoDB;


-- =====================================================
-- 5. COMMENTS
-- =====================================================

CREATE TABLE comments (
    comment_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    text VARCHAR(250) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_comments
        PRIMARY KEY (comment_id),

    INDEX idx_comments_post_created (post_id, created_at),

    CONSTRAINT fk_comments_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id),

    CONSTRAINT fk_comments_post
        FOREIGN KEY (post_id)
        REFERENCES posts(post_id)
        ON DELETE CASCADE
) ENGINE = InnoDB;


-- =====================================================
-- 6. LIKES
-- =====================================================

CREATE TABLE likes (
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_likes
        PRIMARY KEY (user_id, post_id),

    INDEX idx_likes_post (post_id),

    CONSTRAINT fk_likes_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id),

    CONSTRAINT fk_likes_post
        FOREIGN KEY (post_id)
        REFERENCES posts(post_id)
        ON DELETE CASCADE
) ENGINE = InnoDB;


-- =====================================================
-- 7. FOLLOWS
-- =====================================================

CREATE TABLE follows (
    follower_id BIGINT NOT NULL,
    following_id BIGINT NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_follows
        PRIMARY KEY (follower_id, following_id),

    INDEX idx_follows_following (following_id),

    CONSTRAINT chk_follows_no_self_follow
        CHECK (follower_id <> following_id),

    CONSTRAINT fk_follows_follower
        FOREIGN KEY (follower_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_follows_following
        FOREIGN KEY (following_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
) ENGINE = InnoDB;


-- =====================================================
-- 8. REVOKED TOKENS
-- =====================================================

CREATE TABLE revoked_tokens (
    token_id VARCHAR(36) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    expires_at DATETIME NOT NULL,
    revoked_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_revoked_tokens_user_id (user_id),
    INDEX idx_revoked_tokens_expires_at (expires_at),

    CONSTRAINT fk_revoked_tokens_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
) ENGINE = InnoDB;
