package com.hbbhbank.moamoa.user.controller;

import com.hbbhbank.moamoa.user.dto.request.LoginRequestDto;
import com.hbbhbank.moamoa.user.dto.request.SignUpRequestDto;
import com.hbbhbank.moamoa.user.dto.response.LoginResponseDto;
import com.hbbhbank.moamoa.user.dto.response.SignUpResponseDto;
import com.hbbhbank.moamoa.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthService authService;

  @PostMapping("/signup")
  public ResponseEntity<SignUpResponseDto> signUp(@RequestBody @Valid SignUpRequestDto dto) {
    SignUpResponseDto response = authService.signUp(dto);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto loginRequestDto) {
    return ResponseEntity.ok(authService.login(loginRequestDto));
  }
}
