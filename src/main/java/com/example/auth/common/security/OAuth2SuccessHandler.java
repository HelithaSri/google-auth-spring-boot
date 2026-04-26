package com.example.auth.common.security;

import com.example.auth.entities.User;
import com.example.auth.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 1. extract user info from Google
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String googleId = oAuth2User.getAttribute("sub"); // Google's unique user ID
        String avatarUrl = oAuth2User.getAttribute("picture");

        // 2. find or create user in DB
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    System.out.println("Creating new user for email: " + email);
                    // new user — create account
                    User newUser = User.builder()
                            .email(email)
                            .name(name)
                            .googleId(googleId)
                            .avatarUrl(avatarUrl)
                            .authProvider(User.AuthProvider.GOOGLE)
                            .role(User.Role.BUYER)
                            .isVerified(true) // Google already verified email
                            .isActive(true)
                            .build();
                    return userRepository.save(newUser);
                });

        // 3. generate YOUR JWT tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // 4. redirect frontend with tokens in URL
        String redirectUrl = String.format(
                "http://localhost:3000/api/auth/callback?accessToken=%s&refreshToken=%s",
                accessToken, refreshToken
        );

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}