/**
 * 用户注册请求参数 (对应 Java 的 UserRegisterRec)
 * 用于在系统中创建新账号，支持消费者和商户等不同角色的注册
 */
export interface UserRegisterSend {
    /**
     * 用户名 (不能为空)
     */
    userName: string;

    /**
     * 密码 (不能为空)
     */
    password: string;

    /**
     * 用户角色 (不能为空)
     * 只能为: 'consumer' | 'merchant' | 'admin' 对应顾客 商家 管理员 需要在用户注册的时候选择
     */
    userType: string;

    /**
     * 用户头像 URL (必选)
     */
    avatarUrl: string;
}