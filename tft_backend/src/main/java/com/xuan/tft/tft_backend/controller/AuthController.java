package com.xuan.tft.tft_backend.controller;

import com.xuan.tft.tft_backend.dto.AuthResponse;
import com.xuan.tft.tft_backend.dto.UserLoginRequest;
import com.xuan.tft.tft_backend.dto.UserRegisterRequest;
import com.xuan.tft.tft_backend.dto.UserResponseDto;
import com.xuan.tft.tft_backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@RequestBody UserRegisterRequest request) {
        UserResponseDto user = userService.register(request);
        return ResponseEntity.ok(user);
    }

//    @PostMapping("/login")
//    public ResponseEntity<UserResponseDto> login(@RequestBody UserLoginRequest request) {
//        UserResponseDto user = userService.login(request);
//        return ResponseEntity.ok(user);
//    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody UserLoginRequest request) {
        var authResponse = userService.login(request);
        return ResponseEntity.ok(authResponse);
    }

}
