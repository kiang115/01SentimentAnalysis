package org.example.sentimentanalysis.controller;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    @GetMapping("/log")
    public String index() {
//        理解：会在返回的时候将satoken返回给前端
        StpUtil.login("admin");
        return "Greetings from Spring Boot!";
    }

    @GetMapping("/isLogin")
    public String isLogin() {
        return "当前会话是否登录：" + StpUtil.isLogin();
    }
}
