/*
 *  公共接口
 *
 * @Author:    潘多码
 * @Date:      2024-06-11 15:46:03
 * @公众号:     潘多码
 * @Copyright  潘多码 （ http://www.panduoma.com ）
 */

import {post, get} from '@/utils/request';
import type {UserLoginSend} from "@/Dto/SendDto/UserLoginSend.ts";
import type {ApiResponse} from "@/api/model-api.ts";
import type {UserLoginRec} from "@/Dto/ReceiveDto/UserLoginRec.ts";
import type {UserRegisterSend} from "@/Dto/SendDto/UserRegisterSend.ts";
import type {UserLogoutSend} from "@/Dto/SendDto/UserLogoutSend.ts";

export const commonApi = {
    // 登录api
    login: (para: UserLoginSend) => {
        return post('/login', para) as Promise<ApiResponse<UserLoginRec>>;
    },
//     注册api
    register: (para: UserRegisterSend) => {
        return post('/register', para) as Promise<ApiResponse<void>>;
    },
//     退出登录api
    logout: (para: UserLogoutSend) => {
        return post('/logout', para) as Promise<ApiResponse<void>>;
    }
};
