
package com.suryaar.socialmedia.security;

import com.suryaar.socialmedia.entity.User;
import com.suryaar.socialmedia.entity.UserStatus;
import com.suryaar.socialmedia.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final TokenRevocationService tokenRevocationService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository, TokenRevocationService tokenRevocationService) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.tokenRevocationService = tokenRevocationService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            // Parse the token and verify its signature and claims.
            Long userId = jwtService.extractUserId(token);

            if (!jwtService.isTokenValid(token, userId)) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            // Check persistent token revocation.
            String tokenId = jwtService.extractTokenId(token);

            if (tokenRevocationService.isTokenRevoked(tokenId)) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            // Load the account associated with the token.
            Optional<User> userOptional = userRepository.findById(userId);

            if (userOptional.isPresent()
                    && userOptional.get().getStatus() == UserStatus.ACTIVE
                    && SecurityContextHolder.getContext()
                    .getAuthentication() == null) {

                User user = userOptional.get();

                Integer tokenVersion =
                        jwtService.extractTokenVersion(token);

                if (tokenVersion != null
                        && tokenVersion.equals(user.getTokenVersion())) {

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user.getUserId(),
                                    null,
                                    List.of(
                                            new SimpleGrantedAuthority(
                                                    "ROLE_" + user.getRole().name()
                                            )
                                    )
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request));

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }
            }

        } catch (RuntimeException ex) {
            // Malformed, tampered, expired or otherwise invalid JWT.
            SecurityContextHolder.clearContext();
        }

        // Continue unauthenticated; protected routes will return 401.
        filterChain.doFilter(request, response);
    }
}
