package org.example.sentimentanalysis.controller;

import cn.dev33.satoken.stp.StpUtil;
import org.example.sentimentanalysis.response.Response;
import org.springframework.web.bind.annotation.*;

@RestController
public class LoginController {

    @GetMapping("/login")
    @CrossOrigin(origins = "http://localhost:5173")
    public Response<String> index() {
//        理解：会在返回的时候将satoken返回给前端
        StpUtil.login("admin");
        return Response.data(StpUtil.getTokenValue());
    }

    @RequestMapping("/isLogin")
    public String isLogin() {
        return "当前会话是否登录：" + StpUtil.isLogin();
    }
}
