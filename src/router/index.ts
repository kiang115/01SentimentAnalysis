import { createRouter, createWebHistory, type RouteLocationNormalized } from "vue-router";
import { ElMessage } from "element-plus";
import Constants from "@/utils/constants";
import { clearLocalStorage } from "@/utils/utils";
import { userStore } from "@/stores/user";

type UserRole = "admin" | "merchant" | "consumer";

function hasRouteRolePermission(to: RouteLocationNormalized, userType: string) {
  const requiredRoles = to.matched
    .flatMap((record) => {
      const metaRoles = record.meta.roles;
      return Array.isArray(metaRoles) ? metaRoles : [];
    }) as UserRole[];

  if (requiredRoles.length === 0) {
    return true;
  }

  return requiredRoles.includes(userType as UserRole);
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/login",
      name: "login",
      component: () => import("../views/Login.vue"),
    },
    {
      path: "/",
      component: () => import("../views/AppLayout.vue"),
      redirect: { name: "merchants" },
      children: [
        {
          path: "merchants",
          name: "merchants",
          component: () => import("../views/MerchantList.vue"),
        },
        {
          path: "merchant/:merchantId",
          name: "merchantDetail",
          component: () => import("../views/MerchantDetail.vue"),
        },
        {
          path: "product/:productId",
          name: "productDetail",
          component: () => import("../views/ProductDetail.vue"),
        },
        {
          path: "testVue",
          name: "testVue",
          component: () => import("../components/TestVue.vue"),
        },
        {
          path: "model",
          component: () => import("../views/Model.vue"),
          meta: { roles: ["admin"] },
          redirect: { name: "trainData" },
          children: [
            {
              path: "trainData",
              name: "trainData",
              component: () => import("../components/TrainData.vue"),
            },
            {
              path: "modelData",
              name: "modelData",
              component: () => import("../components/ModelData.vue"),
            },
            {
              path: "Task",
              name: "Task",
              component: () => import("../components/Task.vue"),
            },
          ],
        },
      ],
    },
    { path: "/Task", redirect: "/model/Task" },
    { path: "/trainData", redirect: "/model/trainData" },
    { path: "/modelData", redirect: "/model/modelData" },
  ],
});

// ----------------------- 路由加载前 -----------------------
router.beforeEach(async (to, from) => {
  // 登录页面不需要验证直接跳转
  if (to.path === Constants.PAGE_ADMIN_LOGIN) {
    return true;
  }
  // 非登录界面 需要进行鉴权 即看看本地有无token
  const store = userStore();
  const token = store.getToken;
  console.log("token已经找到", token);
  if (!token) {
    clearLocalStorage();
    return { path: "/login" };
  }

  if (!hasRouteRolePermission(to, store.userInfo.userType)) {
    ElMessage.closeAll();
    ElMessage.error("无权访问该页面");
    return { path: "/merchants" };
  }
});

export default router;
