package com.stockmate.controller;

import com.stockmate.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user login and registration")
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    @Operation(summary = "Show Login", description = "Displays the login page")
    public String login(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("errorMsg", "Username atau password salah");
        }
        return "login";
    }

    @GetMapping("/register")
    @Operation(summary = "Show Registration", description = "Displays the registration page")
    public String showRegistrationForm() {
        return "register";
    }

    @PostMapping("/register")
    @Operation(summary = "Register User", description = "Registers a new user in the system")
    public String registerUser(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model
    ) {
        if (username == null || username.trim().isEmpty()) {
            model.addAttribute("errorMsg", "Username tidak boleh kosong");
            return "register";
        }
        if (password == null || password.isEmpty()) {
            model.addAttribute("errorMsg", "Password tidak boleh kosong");
            return "register";
        }
        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMsg", "Password dan konfirmasi password tidak cocok");
            return "register";
        }

        try {
            userService.registerUser(username.trim(), password);
            return "redirect:/login?registered=true";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMsg", e.getMessage());
            return "register";
        }
    }
}
