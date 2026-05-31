package com.udom.securecloud.service;

import com.udom.securecloud.model.PasswordResetToken;
import com.udom.securecloud.model.User;
import com.udom.securecloud.repository.PasswordResetTokenRepository;
import com.udom.securecloud.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PasswordResetServiceTest {

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @Test
    public void testRequestPasswordReset_UserExists() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@udom.ac.tz");

        when(userRepository.findByEmail("test@udom.ac.tz")).thenReturn(Optional.of(user));
        when(tokenRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        boolean result = passwordResetService.requestPasswordReset("test@udom.ac.tz");

        assertTrue(result);
        verify(emailService).sendPasswordResetEmail(eq("test@udom.ac.tz"), any());
    }

    @Test
    public void testRequestPasswordReset_UserNotFound() {
        when(userRepository.findByEmail("unknown@udom.ac.tz")).thenReturn(Optional.empty());

        boolean result = passwordResetService.requestPasswordReset("unknown@udom.ac.tz");

        assertTrue(result); // Should not reveal if email exists
        verify(emailService, never()).sendPasswordResetEmail(any(), any());
    }

    @Test
    public void testResetPassword_ValidToken() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@udom.ac.tz");

        PasswordResetToken token = PasswordResetToken.builder()
                .token("valid-token")
                .user(user)
                .used(false)
                .expiryDate(java.time.LocalDateTime.now().plusHours(1))
                .build();

        when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedPassword");
        when(userRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(tokenRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        boolean result = passwordResetService.resetPassword("valid-token", "newPassword123");

        assertTrue(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testResetPassword_InvalidToken() {
        when(tokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        boolean result = passwordResetService.resetPassword("invalid-token", "newPassword123");

        assertFalse(result);
    }
}
