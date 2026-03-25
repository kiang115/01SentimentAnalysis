import { computed, reactive } from 'vue'
import { defineStore } from 'pinia'
import { getLocalStorage, saveLocalStorage, clearLocalStorage } from '@/utils/utils';
import LocalStorageKeyConst from '@/utils/constants';
import type { UserInfo } from "@/Dto/CommonDto/UserInfo.ts";

function createDefaultUserInfo(): UserInfo {
    return {
        userId: null,
        userName: '',
        userType: '',
        merchantId: null,
        token: '',
        avatarUrl: ''
    };
}

function getStoredUserInfo(): UserInfo {
    const storedUserInfo = getLocalStorage(LocalStorageKeyConst.USER_INFO);

    if (!storedUserInfo) {
        return createDefaultUserInfo();
    }

    try {
        return Object.assign(createDefaultUserInfo(), JSON.parse(storedUserInfo));
    } catch {
        return createDefaultUserInfo();
    }
}

export const userStore = defineStore('user', () => {
    const userInfo = reactive<UserInfo>(getStoredUserInfo())

    function persistUserInfo() {
        saveLocalStorage(LocalStorageKeyConst.USER_INFO, JSON.stringify(userInfo));
    }

    const getToken = computed(() => {
        if (userInfo.token) {
            return userInfo.token;
        }
        return getLocalStorage(LocalStorageKeyConst.USER_TOKEN)
    })

    function setLoginInfo(data: UserInfo) {
        // 直接覆盖
        Object.assign(userInfo, data);

        if (data.token) {
            saveLocalStorage(LocalStorageKeyConst.USER_TOKEN, data.token);
        }

        persistUserInfo();
    }

    function updateMerchantId(merchantId: number) {
        userInfo.merchantId = merchantId;
        persistUserInfo();
    }

    function loginOut() {
        // 重置
        Object.assign(userInfo, createDefaultUserInfo());

        clearLocalStorage();
    }

    return { userInfo, getToken, setLoginInfo, updateMerchantId, loginOut }
})
