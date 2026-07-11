package com.stockmate.controller;

import com.stockmate.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    public void testShowLogin() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    public void testShowLoginWithError() throws Exception {
        mockMvc.perform(get("/login?error=true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("errorMsg"));
    }

    @Test
    public void testShowRegistrationForm() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    public void testRegisterUserSuccess() throws Exception {
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("username", "testuser")
                .param("password", "pass")
                .param("confirmPassword", "pass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered=true"));

        verify(userService, times(1)).registerUser("testuser", "pass");
    }

    @Test
    public void testRegisterUserPasswordMismatch() throws Exception {
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("username", "testuser")
                .param("password", "pass1")
                .param("confirmPassword", "pass2"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("errorMsg"));

        verify(userService, never()).registerUser(anyString(), anyString());
    }

    @Test
    public void testRegisterUserEmptyUsername() throws Exception {
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("username", "   ")
                .param("password", "pass")
                .param("confirmPassword", "pass"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("errorMsg"));
    }

    @Test
    public void testRegisterUserEmptyPassword() throws Exception {
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("username", "testuser")
                .param("password", "")
                .param("confirmPassword", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("errorMsg"));
    }

    @Test
    public void testRegisterUserIllegalArgumentException() throws Exception {
        doThrow(new IllegalArgumentException("Username sudah terpakai")).when(userService).registerUser("testuser", "pass");

        mockMvc.perform(post("/register")
                .with(csrf())
                .param("username", "testuser")
                .param("password", "pass")
                .param("confirmPassword", "pass"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("errorMsg"));
    }
}
