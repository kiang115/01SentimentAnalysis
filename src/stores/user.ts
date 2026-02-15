import {ref, computed, reactive} from 'vue'
import {defineStore} from 'pinia'
import {getLocalStorage, saveLocalStorage, clearLocalStorage} from '@/utils/utils';
import LocalStorageKeyConst from '@/utils/constants';
// store文件夹下的都与pinia.d.ts文件关联
export const userStore = defineStore('user', () => {
    // 定义token和用户实体
    const token = ref('');
    // const user = reactive({
    //     id: '',
    //     username: '',
    //     name: '',
    //     tel: ''
    // })
    //  每次重定向，都调用这里的方法。先检查内存有无token，有则返回，无则从本地浏览器中获取
    const getToken = computed(() => {
        if (token.value) {
            return token.value;
        }
        return getLocalStorage(LocalStorageKeyConst.USER_TOKEN)
    })

    //设置管理员登录信息
    function setLoginInfo(value: any) {
        // user.id = data.id;
        // user.username = data.username;
        // user.name = data.name;
        // user.tel = data.tel;
        token.value = value;
        saveLocalStorage(LocalStorageKeyConst.USER_TOKEN, token.value);
    }

    //退出登录
    function loginOut() {
        token.value = '';
        // user.id = "";
        // user.username = "";
        // user.name = "";
        // user.tel = "";
        clearLocalStorage();
    }

// 类似于exposed，暴露出接口让其他地方使用 
    return {token,  getToken, setLoginInfo, loginOut}
})
