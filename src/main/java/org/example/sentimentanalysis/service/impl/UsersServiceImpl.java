package org.example.sentimentanalysis.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.sentimentanalysis.dto.requestDto.UserLoginRec;
import org.example.sentimentanalysis.dto.requestDto.UserRegisterRec;
import org.example.sentimentanalysis.dto.responseDto.UserLoginSend;
import org.example.sentimentanalysis.enums.UserTypeEnum;
import org.example.sentimentanalysis.exception.CustomBusinessException;
import org.example.sentimentanalysis.mapper.UsersMapper;
import org.example.sentimentanalysis.model.Users;
import org.example.sentimentanalysis.service.UsersService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * <p>
 * 用户信息表 服务实现类
 * </p>
 *
 * @author kiang
 * @since 2026-03-17
 */
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements UsersService {

    private static final Byte ENABLE_STATUS = 1;

    @Override
    public void register(UserRegisterRec rec) {
        // 1) 校验用户名唯一
        LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<Users>()
                .eq(Users::getUserName, rec.getUserName());
        if (this.count(queryWrapper) > 0) {
            throw new CustomBusinessException("用户名已存在");
        }

        // 2) 校验角色合法
        if (!UserTypeEnum.isCodeExist(rec.getUserType())) {
            throw new CustomBusinessException("用户角色不合法");
        }

//        // 3) 校验商家角色的关联商铺信息
//        if (Objects.equals(rec.getUserType(), UserTypeEnum.MERCHANT.getCode()) && rec.getMerchantId() == null) {
//            throw new CustomBusinessException("商家账号必须绑定merchantId");
//        }

        // 4) 组装实体并入库（当前按你的要求：明文直存直比）
        Users user = new Users()
                .setUserName(rec.getUserName())
                .setPasswordHash(rec.getPassword())
                .setAvatarUrl(rec.getAvatarUrl())
                .setUserType(rec.getUserType())
//                .setMerchantId(Objects.equals(rec.getUserType(), UserTypeEnum.MERCHANT.getCode()) ? rec.getMerchantId() : null)
                .setStatus(ENABLE_STATUS);

        if (!this.save(user)) {
            throw new CustomBusinessException("注册失败");
        }
    }

    @Override
    public UserLoginSend login(UserLoginRec rec) {
        // 1) 按用户名查询用户
        Users user = this.getOne(new LambdaQueryWrapper<Users>().eq(Users::getUserName, rec.getUserName()));
        if (user == null) {
            throw new CustomBusinessException("用户名或密码错误");
        }

        // 2) 校验账号状态
        if (!Objects.equals(user.getStatus(), ENABLE_STATUS)) {
            throw new CustomBusinessException("账号已禁用");
        }

        // 3) 密码比对（当前按你的要求：明文直比）
        if (!Objects.equals(user.getPasswordHash(), rec.getPassword())) {
            throw new CustomBusinessException("用户名或密码错误");
        }

        // 4) 建立 Sa-Token 会话
        StpUtil.login(user.getUserId());

        // 5) 更新最后登录时间
        this.updateById(new Users()
                .setUserId(user.getUserId())
                .setLastLoginTime(LocalDateTime.now()));

        // 6) 返回登录态，角色永远以数据库查询结果为准
        return UserLoginSend.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .userType(user.getUserType())
                .merchantId(user.getMerchantId())
                .avatarUrl(user.getAvatarUrl())
                .token(StpUtil.getTokenValue())
                .build();
    }

    @Override
    public void logout(Long userId) {
        if (userId == null) {
            throw new CustomBusinessException("用户ID不能为空");
        }
        // 按 loginId 强制下线指定用户会话
        StpUtil.logout(userId);
    }
}
