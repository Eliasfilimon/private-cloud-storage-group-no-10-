package com.udom.securecloud.service;

import com.udom.securecloud.model.PasswordResetToken;
import com.udom.securecloud.model.User;
import com.udom.securecloud.repository.PasswordResetTokenRepository;
import com.udom.securecloud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public boolean requestPasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Don't reveal if email exists
            return true;
        }

        User user = userOpt.get();

        // Invalidate existing tokens
        tokenRepository.findByUserId(user.getId()).ifPresent(tokenRepository::delete);

        // Create new token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();

        tokenRepository.save(resetToken);

        // Send email
        emailService.sendPasswordResetEmail(email, token);

        return true;
    }

    @Transactional(readOnly = true)
    public boolean validateToken(String token) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = tokenOpt.get();
        return !resetToken.isUsed() && !resetToken.isExpired();
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = tokenOpt.get();
        if (resetToken.isUsed() || resetToken.isExpired()) {
            return false;
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        return true;
    }
}
