package com.stockmate.service;

import com.stockmate.model.User;
import com.stockmate.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    public void setUp() {
        testUser = new User("testuser", "encodedPassword");
    }

    @Test
    public void testFindByUsername() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        Optional<User> found = userService.findByUsername("testuser");
        
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    public void testRegisterUserSuccess() {
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        
        userService.registerUser("newuser", "password");
        
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testRegisterUserAlreadyExists() {
        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(testUser));
        
        assertThrows(RuntimeException.class, () -> {
            userService.registerUser("existinguser", "password");
        });
        
        verify(userRepository, never()).save(any(User.class));
    }
}
