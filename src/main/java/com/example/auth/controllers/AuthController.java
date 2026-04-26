package com.example.auth.controllers;

import com.example.auth.common.response.ApiResponse;
import com.example.auth.common.response.BaseController;
import com.example.auth.models.requests.LoginRequest;
import com.example.auth.models.requests.RegisterRequest;
import com.example.auth.models.responses.AuthResponse;
import com.example.auth.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/")
@RequiredArgsConstructor
public class AuthController extends BaseController {

    private final AuthService authService;

    @PostMapping("register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        return created(authService.register(request), "User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        return ok(authService.login(request), "Login successful");
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<String>> me() {
        return ok("You are authenticated!");
    }
}