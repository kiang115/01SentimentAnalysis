package org.example.sentimentanalysis.controller;

import cn.dev33.satoken.stp.StpUtil;
import org.example.sentimentanalysis.config.StpKit;
import org.example.sentimentanalysis.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
public class LoginController {

    @GetMapping("/login")
    @CrossOrigin(origins = "http://localhost:5173")
    public Response<String> index() {
//        理解：会在返回的时候将satoken返回给前端
        StpKit.ADMIN.login("admin");
        boolean isOk = StpKit.ADMIN.hasPermission("admin.all");
        if(isOk){
            System.out.println("账号具有管理员全部权限");
        }
        System.out.println(StpUtil.getLoginType());
        return Response.data(StpUtil.getTokenValue());
    }

    @RequestMapping("/isLogin")
    public String isLogin() {
        return "当前会话是否登录：" + StpUtil.isLogin();
    }
}
