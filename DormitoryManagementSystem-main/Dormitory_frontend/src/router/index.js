import Layout from '../layout/Layout.vue'
import { createRouter, createWebHistory } from "vue-router";

export const constantRoutes = [
  { path: '/Login', name: 'Login', component: () => import("@/views/Login") },
  {
    path: '/Layout',
    name: 'Layout',
    component: Layout,
    children: [
      { path: '/home', name: 'Home', component: () => import("@/views/Home") },
      { path: '/stuInfo', name: 'StuInfo', component: () => import("@/views/StuInfo") },
      { path: '/dormManagerInfo', name: 'DormManagerInfo', component: () => import("@/views/DormManagerInfo") },
      { path: '/buildingInfo', name: 'BuildingInfo', component: () => import("@/views/BuildingInfo") },
      { path: '/roomInfo', name: 'RoomInfo', component: () => import("@/views/RoomInfo") },
      { path: '/noticeInfo', name: 'NoticeInfo', component: () => import("@/views/NoticeInfo") },
      { path: '/adjustRoomInfo', name: 'AdjustRoomInfo', component: () => import("@/views/AdjustRoomInfo") },
      { path: '/repairInfo', name: 'RepairInfo', component: () => import("@/views/RepairInfo") },
      { path: '/visitorInfo', name: 'VisitorInfo', component: () => import("@/views/VisitorInfo") },
      { path: '/myRoomInfo', name: 'MyRoomInfo', component: () => import("@/views/MyRoomInfo") },
      { path: '/applyRepairInfo', name: 'ApplyRepairInfo', component: () => import("@/views/ApplyRepairInfo") },
      { path: '/applyChangeRoom', name: 'ApplyChangeRoom', component: () => import("@/views/ApplyChangeRoom") },
      // 新增：师生共用的教室查询路由（单页面）
      { 
        path: '/classroomQuery', 
        name: 'ClassroomQuery', 
        component: () => import("@/views/ClassroomQuery") 
      },
      { 
  path: '/classroomApproval', 
  name: 'ClassroomApproval', 
  component: () => import("@/views/ClassroomApproval") 
},
      { path: '/selfInfo', name: 'SelfInfo', component: () => import("@/views/SelfInfo") },
      { path: '/teacherInfo', name: 'TeacherInfo', component: () => import("@/views/TeacherInfo") }
    ]
  },
  { path: '/', redirect: '/Login' }
]

const router = createRouter({
  routes: constantRoutes,
  history: createWebHistory(process.env.BASE_URL)
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const user = window.sessionStorage.getItem('user')
  if (to.path === '/Login') return next();
  if (!user) return next('/Login');
  if (to.path === '/' && user) return next('/home');
  next();
})

export default router;