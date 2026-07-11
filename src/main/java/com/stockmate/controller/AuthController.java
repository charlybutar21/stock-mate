package com.stockmate.controller;

import com.stockmate.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("errorMsg", "Username atau password salah");
        }
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    @PostMapping("/register")
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
