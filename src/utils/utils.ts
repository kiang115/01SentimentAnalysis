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

export const getFileUrl = (url: string) => {
    return Constants.BASE_URL + '/' + url;
};


export const urls2Filelist = (url: any) => {
    const tlist: any = [];
    if (url) {
        url.split(',').map((item: string) => {
            let url = getFileUrl(item);
            const file = {
                name: item,
                url: url
            }
            tlist.push(file);
        });
    }
    return tlist;
};

export const fileUrls2Filelist = (url: any) => {
    const tlist: any = [];
    if (url) {
        url.split(',').map((item: string) => {
            let url = getFileUrl(item);
            const file = {
                name: item.substring(item.lastIndexOf('upload') + "upload".length + 1),
                url: url
            }
            tlist.push(file);
        });
    }
    return tlist;
};