/// <reference types="vite/client" />
/// <reference types="vite/client" />
declare module 'element-plus'
declare module '*.vue' {
    import {ComponentOptions} from 'vue'
    const componentOptions: ComponentOptions
    export default componentOptions
}
