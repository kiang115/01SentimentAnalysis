package org.example.sentimentanalysis.config;

import cn.dev33.satoken.stp.StpInterface;
import org.example.sentimentanalysis.enums.UserTypeEnum;
import org.example.sentimentanalysis.model.Users;
import org.example.sentimentanalysis.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StpInterfaceImpl implements StpInterface {

    @Autowired
    private UsersService usersService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 当前项目先基于角色做接口鉴权，不做细粒度权限点控制
        return List.of();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        if (loginId == null) {
            return List.of();
        }
        Long userId;
        if (loginId instanceof Number number) {
            userId = number.longValue();
        } else {
            try {
                userId = Long.valueOf(String.valueOf(loginId));
            } catch (NumberFormatException e) {
                return List.of();
            }
        }

        Users user = usersService.getById(userId);
        if (user == null || !UserTypeEnum.isCodeExist(user.getUserType())) {
            return List.of();
        }
        return List.of(user.getUserType());
    }
}
