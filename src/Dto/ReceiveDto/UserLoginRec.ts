/**
 * 用户登录成功返回结果 (对应 Java 的 UserLoginSend)
 * 包含了用户基础信息、角色权限以及用于后续请求鉴权的 Token
 */
export interface UserLoginRec {
    /**
     * 用户唯一标识 ID
     */
    userId: number;

    /**
     * 用户登录名
     */
    userName: string;

    /**
     * 用户类型/角色
     * 'consumer' (消费者), 'merchant' (商户), 'admin' (管理员)
     */
    userType: string;

    /**
     * 关联的商家 ID
     * 若用户类型为 'merchant'，则此字段返回其所属的商家 ID；否则通常为 null
     */
    merchantId: number | null;

    /**
     * 身份鉴权 Token (通常为 JWT)
     * 前端需将其存储在 LocalStorage 或 Cookie 中，并在后续请求的 Header 中携带
     */
    token: string;

//     用户头像url
    avatarUrl: string;
}