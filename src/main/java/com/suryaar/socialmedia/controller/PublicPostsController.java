
package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.dto.PublicPostResponse;
import com.suryaar.socialmedia.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class PublicPostsController {

    private final UserService userService;

    public PublicPostsController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{username}/posts")
    public ResponseEntity<Page<PublicPostResponse>> getPublicPosts(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<PublicPostResponse> posts =
                userService.getPublicPosts(username, page, size);

        return ResponseEntity.ok(posts);
    }
}
