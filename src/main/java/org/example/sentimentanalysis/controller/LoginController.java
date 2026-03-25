package org.example.sentimentanalysis.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.example.sentimentanalysis.dto.requestDto.UserLoginRec;
import org.example.sentimentanalysis.dto.requestDto.UserLogoutRec;
import org.example.sentimentanalysis.dto.requestDto.UserRegisterRec;
import org.example.sentimentanalysis.dto.responseDto.UserLoginSend;
import org.example.sentimentanalysis.response.Response;
import org.example.sentimentanalysis.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    @Autowired
    private UsersService usersService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Response<Void> register(@RequestBody @Valid UserRegisterRec rec) {
        usersService.register(rec);
        return Response.success();
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Response<UserLoginSend> login(@RequestBody @Valid UserLoginRec rec) {
        return Response.data(usersService.login(rec));
    }

    @Operation(summary = "用户退出登录")
    @PostMapping("/logout")
    public Response<Void> logout(@RequestBody @Valid UserLogoutRec rec) {
        usersService.logout(rec.getUserId());
        return Response.success();
    }
}
