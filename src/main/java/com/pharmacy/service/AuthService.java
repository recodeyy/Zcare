package com.pharmacy.service;

import com.pharmacy.dto.AuthRequest;
import com.pharmacy.dto.AuthResponse;
import com.pharmacy.dto.RegisterRequest;
import com.pharmacy.model.User;
import com.pharmacy.repository.UserRepository;
import com.pharmacy.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.lang.NonNull;

@Service
@RequiredArgsConstructor
public class AuthService {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtUtil jwtUtil;
        private final AuthenticationManager authenticationManager;

        public AuthResponse register(@NonNull RegisterRequest request) {
                // Check for duplicate username
                if (userRepository.existsByUsername(request.getUsername())) {
                        throw new DataIntegrityViolationException("Username already exists");
                }

                // Check for duplicate email
                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new DataIntegrityViolationException("Email already exists");
                }

                User user = User.builder()
                                .username(request.getUsername())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .fullName(request.getFullName())
                                .email(request.getEmail())
                                .role(User.Role.PHARMACIST) // Always force initial role as PHARMACIST
                                .active(true)
                                .build();

                User savedUser = java.util.Objects.requireNonNull(userRepository.save(user));
                String jwtToken = jwtUtil.generateToken(savedUser);

                return AuthResponse.builder()
                                .token(jwtToken)
                                .username(savedUser.getUsername())
                                .role(savedUser.getRole().name())
                                .build();
        }

        public AuthResponse authenticate(@NonNull AuthRequest request) {
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.getUsername(),
                                                request.getPassword()));
                User user = userRepository.findByUsername(request.getUsername())
                                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

                String jwtToken = jwtUtil.generateToken(user);
                return AuthResponse.builder()
                                .token(jwtToken)
                                .username(user.getUsername())
                                .role(user.getRole().name())
                                .build();
        }
}
