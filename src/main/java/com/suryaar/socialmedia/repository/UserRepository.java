
package com.suryaar.socialmedia.repository;

import com.suryaar.socialmedia.entity.User;
import com.suryaar.socialmedia.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsernameAndStatus(
            String username, UserStatus status);

    boolean existsByEmailAndStatus(
            String email, UserStatus status);

    boolean existsByPhoneAndStatus(
            String phone, UserStatus status);

    Optional<User> findByUsernameAndStatus(
            String username, UserStatus status);

    Optional<User> findByEmailAndStatus(
            String email, UserStatus status);


    @Modifying
    @Transactional
    @Query("""
    UPDATE User u
    SET u.username = null,
        u.email = null,
        u.phone = null,
        u.password = null,
        u.profilePicture = null,
        u.status = com.suryaar.socialmedia.entity.UserStatus.DELETED,
        u.tokenVersion = u.tokenVersion + 1
    WHERE u.userId = :userId
      AND u.status = com.suryaar.socialmedia.entity.UserStatus.ACTIVE
    """)
    int softDeleteUser(@Param("userId") Long userId);

}
