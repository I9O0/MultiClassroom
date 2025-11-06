<template>
  <el-menu
    :default-active="path"
    router
    style="width: 200px; height: 100%; min-height: calc(100vh - 40px)"
    unique-opened
  >
    <!-- 系统Logo -->
    <div style="display: flex; align-items: center; justify-content: center; padding: 11px 0;">
      <img src="@/assets/logo.png" alt="系统logo" style="width: 60px;">
    </div>

    <!-- 首页 -->
    <el-menu-item index="/home">
      <el-icon><House /></el-icon>
      <span>首页</span>
    </el-menu-item>

    <!-- 用户管理子菜单（管理员/宿管可见） -->
    <el-sub-menu v-if="judgeIdentity() !== 0" index="2">
      <template #title>
        <el-icon><User /></el-icon>
        <span>用户管理</span>
      </template>
      <el-menu-item v-if="judgeIdentity() !== 0" index="/stuInfo">学生信息</el-menu-item>
      <el-menu-item v-if="judgeIdentity() === 2" index="/dormManagerInfo">宿管信息</el-menu-item>
      <el-menu-item v-if="judgeIdentity() === 1 || judgeIdentity() === 2" index="/teacherInfo">教师信息</el-menu-item>
    </el-sub-menu>

    <!-- 宿舍管理子菜单（管理员/宿管可见） -->
    <el-sub-menu v-if="judgeIdentity() !== 0" index="3">
      <template #title>
        <el-icon><Coin /></el-icon>
        <span>宿舍管理</span>
      </template>
      <el-menu-item v-if="judgeIdentity() !== 0" index="/buildingInfo">楼宇信息</el-menu-item>
      <el-menu-item v-if="judgeIdentity() !== 0" index="/roomInfo">房间信息</el-menu-item>
    </el-sub-menu>

    <!-- 信息管理子菜单（管理员/宿管可见） -->
    <el-sub-menu v-if="judgeIdentity() !== 0" index="4">
      <template #title>
        <el-icon><Message /></el-icon>
        <span>信息管理</span>
      </template>
      <el-menu-item v-if="judgeIdentity() === 2" index="/noticeInfo">公告信息</el-menu-item>
      <el-menu-item v-if="judgeIdentity() !== 0" index="/repairInfo">报修信息</el-menu-item>
    </el-sub-menu>

    <!-- 申请管理子菜单（管理员/宿管可见） -->
  <!-- 申请管理子菜单（管理员/宿管可见） -->
<el-sub-menu v-if="judgeIdentity() !== 0" index="5">
  <template #title>
    <el-icon><PieChart /></el-icon>
    <span>申请管理</span>
  </template>
  <el-menu-item v-if="judgeIdentity() !== 0" index="/adjustRoomInfo">调宿申请</el-menu-item>
  <!-- 新增：教室预约审批菜单 -->
  <el-menu-item v-if="judgeIdentity() !== 0" index="/classroomApproval">教室预约审批</el-menu-item>
</el-sub-menu>

    <!-- 访客管理（管理员/宿管可见） -->
    <el-menu-item v-if="judgeIdentity() !== 0" index="/visitorInfo">
      <svg class="icon" style="height: 18px; margin-right: 11px;" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
        <path d="M512 160c320 0 512 352 512 352S832 864 512 864 0 512 0 512s192-352 512-352zm0 64c-225.28 0-384.128 208.064-436.8 288 52.608 79.872 211.456 288 436.8 288 225.28 0 384.128-208.064 436.8-288-52.608-79.872-211.456-288-436.8-288zm0 64a224 224 0 110 448 224 224 0 010-448zm0 64a160.192 160.192 0 00-160 160c0 88.192 71.744 160 160 160s160-71.808 160-160-71.744-160-160-160z" fill="currentColor"></path>
      </svg>
      <span>访客管理</span>
    </el-menu-item>

    <!-- 学生专属菜单（仅学生可见） -->
    <el-menu-item v-if="judgeIdentity() === 0" index="/myRoomInfo">
      <el-icon><School /></el-icon>
      <span>我的宿舍</span>
    </el-menu-item>
    <el-menu-item v-if="judgeIdentity() === 0" index="/applyChangeRoom">
      <el-icon><TakeawayBox /></el-icon>
      <span>申请调宿</span>
    </el-menu-item>
    <el-menu-item v-if="judgeIdentity() === 0" index="/applyRepairInfo">
      <el-icon><SetUp /></el-icon>
      <span>报修申请</span>
    </el-menu-item>
    <!-- 新增：学生端教室预约菜单 -->
    <el-menu-item v-if="judgeIdentity() === 0" index="/classroomQuery">
      <el-icon><OfficeBuilding /></el-icon>
      <span>教室预约</span>
    </el-menu-item>

    <!-- 个人信息（所有角色可见） -->
    <el-menu-item index="/selfInfo">
      <el-icon><Setting /></el-icon>
      <span>个人信息</span>
    </el-menu-item>
  </el-menu>
</template>

<script>
import request from "@/utils/request";
import { ElMessage } from "element-plus";
// 导入Element Plus图标（新增OfficeBuilding图标用于教室预约）
import { 
  House, User, Coin, Message, PieChart, 
  School, TakeawayBox, SetUp, Setting, OfficeBuilding 
} from '@element-plus/icons-vue';

export default {
  name: "Aside",
  components: {
    // 注册图标组件（新增OfficeBuilding）
    House, User, Coin, Message, PieChart,
    School, TakeawayBox, SetUp, Setting, OfficeBuilding
  },
  data() {
    return {
      user: {}, // 用户信息
      identity: '', // 身份标识（stu/dormManager/admin等）
      path: this.$route.path // 当前路由路径（用于菜单高亮）
    };
  },
  created() {
    this.init(); // 初始化加载用户信息和身份
  },
  methods: {
    // 初始化：加载用户身份和信息（优化异步顺序）
    async init() {
      try {
        // 1. 先加载身份标识（必须先获取身份才能控制菜单显示）
        const identityRes = await request.get("/main/loadIdentity");
        if (identityRes.code !== "0") {
          throw new Error("加载身份失败");
        }
        this.identity = identityRes.data;
        window.sessionStorage.setItem("identity", JSON.stringify(this.identity));

        // 2. 再加载用户信息
        const userRes = await request.get("/main/loadUserInfo");
        if (userRes.code !== "0") {
          throw new Error("加载用户信息失败");
        }
        this.user = userRes.data;
        window.sessionStorage.setItem("user", JSON.stringify(this.user));
      } catch (err) {
        ElMessage.error(err.message || '用户会话过期，请重新登录');
        sessionStorage.clear();
        request.get("/main/signOut");
        this.$router.replace("/login");
      }
    },

    // 判断用户身份（返回权限等级）
    judgeIdentity() {
      if (this.identity === 'stu'|| this.identity === 'teacher') {
        return 0; // 学生/教师：权限等级0（学生仅能看到自己的菜单）
      } else if (this.identity === 'dormManager') {
        return 1; // 宿管：权限等级1
      } else {
        return 2; // 管理员（默认其他身份）：权限等级2
      }
    }
  }
};
</script>

<style scoped>
/* 访客管理图标样式调整 */
.icon {
  margin-right: 6px;
  vertical-align: middle; /* 与文字垂直居中对齐 */
}

/* 子菜单样式优化 */
.el-sub-menu .el-menu-item {
  height: 50px;
  line-height: 50px;
  padding: 0 45px;
  min-width: 199px;
}

/* 菜单选中样式（可选） */
.el-menu-item.is-active {
  background-color: #f0f7ff !important;
  color: #165dff !important;
}
</style>