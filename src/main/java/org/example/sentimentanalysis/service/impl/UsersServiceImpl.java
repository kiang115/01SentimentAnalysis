package org.example.sentimentanalysis.service.impl;

import org.example.sentimentanalysis.model.Users;
import org.example.sentimentanalysis.mapper.UsersMapper;
import org.example.sentimentanalysis.service.UsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}
