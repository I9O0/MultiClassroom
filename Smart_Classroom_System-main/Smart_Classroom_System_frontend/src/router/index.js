import Layout from '../layout/Layout.vue'
import { createRouter, createWebHistory } from "vue-router";
import { ElMessage } from 'element-plus' // 导入ElMessage用于权限提示

export const constantRoutes = [
  { path: '/Login', name: 'Login', component: () => import("@/views/Login") },
  {
    path: '/Layout',
    name: 'Layout',
    component: Layout,
    children: [
      { path: '/home', name: 'Home', component: () => import("@/views/Home") },
      { path: '/stuInfo', name: 'StuInfo', component: () => import("@/views/StuInfo") },
      { path: '/managerInfo', name: 'managerInfo', component: () => import("@/views/managerInfo") },
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
        component: () => import("@/views/ClassroomQuery"),
        meta: { 
          requireAuth: true, 
          roles: ['stu', 'teacher', 'manager', 'admin'] 
        }
      },
      { 
        path: '/classroomApproval', 
        name: 'ClassroomApproval', 
        component: () => import("@/views/ClassroomApproval"),
        meta: { 
          requireAuth: true,
          roles: ['manager', 'admin'] 
        }
      },
      // 新增：管理员教室管理路由
      { 
        path: '/classroomManager', 
        name: 'ClassroomManager', 
        component: () => import("@/views/ClassroomManager"),
        meta: { 
          requireAuth: true, 
          roles: ['admin','manager'] 
        }
      },
      // 新增：签到签退记录查看路由（管理员和宿管可见）
      { 
        path: '/checkInOutRecords', 
        name: 'CheckInOutRecords', 
        component: () => import("@/views/CheckInOutRecords"),
        meta: { 
          requireAuth: true, 
          roles: ['manager', 'admin']  // 仅管理员和宿管可见
        }
      },
      // 教室报修相关路由
{ 
  path: '/classroomRepairManage', 
  name: 'ClassroomRepairManage', 
  component: () => import("@/views/ClassroomRepairManage"),
  meta: { 
    requireAuth: true, 
    roles: ['manager', 'admin']  // 仅管理员和宿管可见（对应后端canHandleRepair权限控制）
  } 
},
{ 
  path: '/classroomRepairSubmit', 
  name: 'ClassroomRepairSubmit', 
  component: () => import("@/views/ClassroomRepairSubmit"),
  meta: { 
    requireAuth: true, 
    roles: ['stu', 'teacher']  // 学生和教师可见（对应后端canSubmitRepair权限控制）
  } 
},
{ 
  path: '/myClassroomRepairs', 
  name: 'MyClassroomRepairs', 
  component: () => import("@/views/MyClassroomRepairs"),
  meta: { 
    requireAuth: true, 
    roles: ['stu', 'teacher']  // 学生和教师的个人报修记录（对应后端myRecords接口权限）
  } 
},
      // 新增：师生共用的预约记录与签到签退页面
      { 
        path: '/myClassroomReservations', 
        name: 'MyClassroomReservations', 
        component: () => import("@/views/MyClassroomReservations"),
        meta: { 
          requireAuth: true, 
          roles: ['stu', 'teacher']  // 仅学生和老师可见
        }
      },
      { path: '/selfInfo', name: 'SelfInfo', component: () => import("@/views/SelfInfo") },
      { path: '/teacherInfo', name: 'TeacherInfo', component: () => import("@/views/TeacherInfo") },
    ]
  },
  { path: '/', redirect: '/Login' }
]

const router = createRouter({
  routes: constantRoutes,
  history: createWebHistory(process.env.BASE_URL)
})

// 路由守卫（已补充角色校验）
router.beforeEach((to, from, next) => {
  const user = window.sessionStorage.getItem('user');
  const identity = JSON.parse(window.sessionStorage.getItem('identity') || '""');

  // 未登录强制跳转登录页
  if (to.meta.requireAuth && !user) {
    return next('/Login');
  }

  // 已登录但访问登录页，跳转首页
  if (to.path === '/Login' && user) {
    return next('/home');
  }

  // 角色权限校验（如果路由有roles限制）
  if (to.meta.roles && !to.meta.roles.includes(identity)) {
    ElMessage.error('无权限访问该页面');
    return next(from.path || '/home');
  }

  // 无权限限制或校验通过，放行
  next();
})

export default router;