package com.xuan.tft.tft_backend.service;

import com.xuan.tft.tft_backend.dto.AuthResponse;
import com.xuan.tft.tft_backend.dto.UserLoginRequest;
import com.xuan.tft.tft_backend.dto.UserRegisterRequest;
import com.xuan.tft.tft_backend.dto.UserResponseDto;
import com.xuan.tft.tft_backend.entity.User;
import com.xuan.tft.tft_backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }


    public UserResponseDto register(UserRegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty()
                && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("邮箱已被使用");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(encodedPassword);
        user.setRole("USER");

        User saved = userRepository.save(user);
        return toDto(saved);
    }

//    public UserResponseDto login(UserLoginRequest request) {
//        User user = userRepository.findByUsername(request.getUsername())
//                .orElseThrow(() -> new IllegalArgumentException("用户名或密码错误"));
//
//        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
//            throw new IllegalArgumentException("用户名或密码错误");
//        }
//
//        // 暂不签发 JWT，返回基本用户信息
//        return toDto(user);
//    }
    public AuthResponse login(UserLoginRequest request) {
    User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("用户名或密码错误"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        throw new IllegalArgumentException("用户名或密码错误");
    }

    String token = jwtService.generateToken(user.getUsername());
    UserResponseDto userDto = toDto(user);

    return new AuthResponse(token, userDto);
}


    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + id));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + username));
    }


    private UserResponseDto toDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }
}
