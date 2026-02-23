import {createRouter, createWebHistory} from 'vue-router'
import Constants from '@/utils/constants';
import {getLocalStorage, clearLocalStorage} from '@/utils/utils'
import {userStore} from '@/stores/user';

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/login',
            name: 'login',
            component: () => import('../views/Login.vue'),
        },
        {
            path: '/model',
            name: 'model',
            component: () => import('../views/Model.vue'),
        },
        {
            path: '/data',
            name: 'trainData',
            component: () => import('../views/TrainData.vue'),
        }
    ]
})

// ----------------------- 路由加载前 -----------------------
router.beforeEach(async (to, from) => {
    // 登录页面不需要验证直接跳转
    if (to.path === Constants.PAGE_ADMIN_LOGIN) {
        return true;
    }
    // 非登录界面 需要进行鉴权 即看看本地有无token
    const token = userStore().getToken
    console.log('token已经找到', token)
    if (!token) {
        clearLocalStorage();
        return {path: '/login'}
    }
})


export default router
