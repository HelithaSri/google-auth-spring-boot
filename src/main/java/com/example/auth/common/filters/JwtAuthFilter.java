package com.example.auth.common.filters;

import com.example.auth.common.security.JwtService;
import com.example.auth.entities.User;
import com.example.auth.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. get Authorization header
        String authHeader = request.getHeader("Authorization");

        // 2. if no token, skip filter — let SecurityConfig handle it
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. extract token
        String token = authHeader.substring(7);

        try {
            // 4. extract email from token
            String email = jwtService.extractEmail(token);

            // 5. only authenticate if not already authenticated
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 6. load user from DB
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                // 7. validate token
                if (jwtService.isTokenValid(token, user) && jwtService.isAccessToken(token)) {

                    // 8. set authentication in security context
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // invalid token — just continue without setting authentication
            // SecurityConfig will reject the request if endpoint is protected
        }

        filterChain.doFilter(request, response);
    }
}
