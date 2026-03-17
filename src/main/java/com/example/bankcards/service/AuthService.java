package com.example.bankcards.service;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse authenticate(AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            log.warn("Failed authentication attempt for user: {}", request.getUsername());
            throw new BadCredentialsException("Invalid username or password");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        User user = userRepository.findByUsername(request.getUsername()).orElseThrow();

        log.info("User {} authenticated successfully", request.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        String username = jwtService.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        String newAccessToken = jwtService.generateToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .username(username)
                .role(userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""))
                .build();
    }
}

