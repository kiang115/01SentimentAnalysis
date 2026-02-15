// /*
//  *  管理员信息
//  *
//  * @Author:    潘多码
//  * @Date:      2024-06-06 20:46:03
//  * @公众号:     潘多码
//  * @Copyright  潘多码 （ http://www.panduoma.com ）
//  */
//
// import { post, get } from '@/utils/request';
// export const adminApi = {
//     // 新增管理员信息
//     add: (param: any) => {
//         return post('/admin/add', param);
//     },
//     // 修改管理员信息
//     update: (param: any) => {
//         return post('/admin/update', param);
//     },
//     // 删除管理员信息
//     delete: (ids: any) => {
//         return post('/admin/delete?ids=' + ids, {});
//     },
//     // 检验用户名是否唯一
//     checkUsername: (param: any) => {
//         return get('/admin/checkUsername', param);
//     },
//     // 分页查询管理员信息列表
//     queryPageList: (param: any, pagenum: number, pagesize: number) => {
//         return post('/admin/list?pagenum=' + pagenum + '&pagesize=' + pagesize, param);
//     },
//
//     // 登录
//     login: (param: any) => {
//         return post('/admin/login', param);
//     },
//
//     // 退出登录
//     loginout: () => {
//         return post('/admin/loginout', {});
//     },
//
//     // 重置密码
//     resetPwd: (id: any) => {
//         return post('/admin/resetPwd?id=' + id, {});
//     },
//
//
// };