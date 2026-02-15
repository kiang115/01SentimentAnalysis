/*
 *  公共接口
 *
 * @Author:    潘多码
 * @Date:      2024-06-11 15:46:03
 * @公众号:     潘多码
 * @Copyright  潘多码 （ http://www.panduoma.com ）
 */

import {post, get} from '@/utils/request';

export const commonApi = {
    // // 上传文件
    // uploadFile: (param: any) => {
    //     return post('/common/uploadFile', param);
    // },
    // // 下载文件
    // downFile: (param: any) => {
    //     window.location.href = import.meta.env.VITE_APP_API_URL + '/common/downFile?filePath=' + param;
    // },
    //
    //
    // //获取验证码
    // getCaptcha: () => {
    //     return get('/common/getCaptcha', {});
    // },
    login: () => {
        return get('/login', {});
    },
};
