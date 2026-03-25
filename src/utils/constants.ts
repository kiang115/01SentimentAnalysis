/*
 * 
 * 常量 配置类
 * @Author:    潘多码
 * @Date:      2024-06-06 21:46:03
 * @公众号:     潘多码
 * @Copyright  潘多码 （ http://www.panduoma.com ） 
 */

 
export default {
    USER_TOKEN :'token_value', //后端会返回一个token的值，前段需要存储这个token，这里的token_value就是存储在本地的token的变量名字。
    USER_INFO: 'user_info',
    BASE_URL : import.meta.env.VITE_APP_API_URL,
    PAGE_SIZE : 10,
    PAGE_ADMIN_LOGIN : '/login'
}
 
