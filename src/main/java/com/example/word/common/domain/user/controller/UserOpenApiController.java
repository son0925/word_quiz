package com.example.word.common.domain.user.controller;

import com.example.word.common.api.Api;
import com.example.word.common.domain.user.business.UserBusiness;
import com.example.word.common.domain.user.model.LoginRequest;
import com.example.word.common.domain.user.model.UserRegisterRequest;
import com.example.word.common.domain.user.model.UserResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/open-api/user")
public class UserOpenApiController {

    private final UserBusiness userBusiness;

    @GetMapping("")
    public String test() {
        return "test";
    }

    // 로그인
    @PostMapping("/login")
    public Api<UserResponse> login(
            @Valid
            @RequestBody
            Api<LoginRequest> user,
            HttpServletResponse response
    ) {
        return Api.OK(userBusiness.login(user, response));
    }

    // 회원가입
    @PostMapping("/register")
    public Api<UserResponse> register(
            @Valid
            @RequestBody
            Api<UserRegisterRequest> user
    ) {
        return Api.OK(userBusiness.register(user));
    }

    // 아이디 중복 여부 체크
    @PostMapping("/existentID")
    public Api<String> existentID(
            @RequestBody
            Api<LoginRequest> loginRequest
    ) {
        return Api.OK(userBusiness.existentUserWithThrow(loginRequest.getBody().getUserId()));
    }


    // 휴먼 계정 풀기
    @PostMapping("/account-activation")
    public Api<UserResponse> accountActivation(
            @Valid
            @RequestBody
            LoginRequest loginRequest
    ) {
        return Api.OK(userBusiness.accountActivation(loginRequest));
    }

}
