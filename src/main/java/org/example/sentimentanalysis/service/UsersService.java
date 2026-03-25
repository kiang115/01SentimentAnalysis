package org.example.sentimentanalysis.service;

import org.example.sentimentanalysis.dto.requestDto.UserLoginRec;
import org.example.sentimentanalysis.dto.requestDto.UserRegisterRec;
import org.example.sentimentanalysis.dto.responseDto.UserLoginSend;
import org.example.sentimentanalysis.model.Users;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户信息表 服务类
 * </p>
 *
 * @author kiang
 * @since 2026-03-17
 */
public interface UsersService extends IService<Users> {

    void register(UserRegisterRec rec);

    UserLoginSend login(UserLoginRec rec);

    void logout(Long userId);
}
