/*
 *  封装 ajax 请求
 *
 * @Author:    潘多码
 * @Date:      2024-06-06 20:46:03
 * @公众号:     潘多码
 * @Copyright  潘多码 （ http://www.panduoma.com ） 
 */

import axios, {type AxiosRequestConfig} from 'axios';
import {saveLocalStorage, getLocalStorage, clearLocalStorage} from '@/utils/utils';
import Constants from '@/utils/constants';
import {ElMessage, ElMessageBox} from 'element-plus';
import {userStore} from '@/stores/user'

// token的名称  需要和服务端配置的对应
const TOKEN_NAME = 'satoken';

// 创建axios对象
const request = axios.create({
    baseURL: Constants.BASE_URL,
});

// 退出系统
function logout() {
    clearLocalStorage();
    location.href = '/';
}


// ================================= 请求拦截器 =================================
request.interceptors.request.use(
    (config) => {
        // 在发送请求之前消息头加入本地存储的token 后端可以使用saToken鉴权
        // const token = getLocalStorage(Constants.USER_TOKEN);
        const token = userStore().getToken
        if (token) {
            config.headers[TOKEN_NAME] = token;
        } else {
            delete config.headers[TOKEN_NAME];
        }
        return config;
    },
    (error: any) => {
        return Promise.reject(error);
    }
);

// ================================= 响应拦截器 =================================

// 添加响应拦截器
// 判断code

// - 正确200-> 返回干净的后端data ->进入业务代码的then()
//  错误->返回 有网络请求头的完整的response ->进入业务代码catch()（）(reject部分)
request.interceptors.response.use(
    (httpResponse) => {


        // 根据content-type ，判断是否为 json 数据，不是直接返回
        let contentType = httpResponse.headers['content-type'] ? httpResponse.headers['content-type'] : httpResponse.headers['Content-Type'];
        if (contentType.indexOf('application/json') === -1) {
            return Promise.resolve(httpResponse);
        }

        // 如果是json数据
        // 如果写的是json，但是实际上是二进制数据，拒绝接收
        if (httpResponse.data && httpResponse.data instanceof Blob) {
            return Promise.reject(httpResponse.data);
        }

        const responseData = httpResponse.data;
        if (responseData.code && responseData.code !== 200) {
            // `token` 过期或者账号已在别处登录
            if (responseData.code === 11012 || responseData.code == 11011) {
                ElMessage.closeAll();
                ElMessage.error('您没有登录，请重新登录');
                setTimeout(logout, 300);
                return Promise.reject(httpResponse);
            }
            // 长时间未操作系统，需要重新登录 #todo 后端应该设置一个异常处理类，捕获校验异常
            if (responseData.code === 30001) {
                ElMessageBox.confirm('您需要重新登陆', '确认退出登录', {
                    confirmButtonText: '重新登陆',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    logout()
                })
                setTimeout(logout, 3000);
                return Promise.reject(httpResponse);
            }
            ElMessage.closeAll();
            if (responseData.message) {
                ElMessage.error(responseData.message);
            }
            return Promise.reject(httpResponse);
        } else {
            return Promise.resolve(responseData);
        }
    },
    (error) => {
        // 对响应错误做点什么
        if (error.message.indexOf('timeout') !== -1) {
            ElMessage.closeAll();
            ElMessage.error('网络超时');
        } else if (error.message === 'Network Error') {
            ElMessage.closeAll();
            ElMessage.error('网络连接错误');
        } else if (error.message.indexOf('Request') !== -1) {
            ElMessage.closeAll();
            ElMessage.error('网络发生错误');
        }
        return Promise.reject(error);
    }
);

// 


// =================================  通用请求，get， post  =================================

/**
 * 通用请求封装
 * @param config
 */
export const http = (config: AxiosRequestConfig<any>) => {
    return request.request(config);
};

/**
 * get请求
 */
export const get = (url: string, params: any) => {
    return http({url, method: 'get', params});
};
// get('/api/users', { page: 1, size: 10 })

/**
 * post请求
 */
export const post = (url: string, data: {}) => {
    return http({
        data,
        url,
        method: 'post',
    });
};
// post('/api/users', { name: '张三', age: 18 })