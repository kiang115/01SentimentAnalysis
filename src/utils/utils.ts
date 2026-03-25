/*
 * 
 * 工具方法
 * @Author:    潘多码
 * @Date:      2024-06-06 21:46:03
 * @公众号:     潘多码
 * @Copyright  潘多码 （ http://www.panduoma.com ） 
 */
import Constants from '@/utils/constants';

export const saveLocalStorage = (key: string, value: string) => {
    localStorage.setItem(key, value);
};

export const getLocalStorage = (key: string) => {
    return localStorage.getItem(key) || '';
};

export const clearLocalStorage = () => {
    localStorage.clear();
};