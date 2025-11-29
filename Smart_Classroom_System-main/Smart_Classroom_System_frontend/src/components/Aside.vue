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
      <el-menu-item v-if="judgeIdentity() === 2" index="/managerInfo">教室管理员信息</el-menu-item>
      <el-menu-item v-if="judgeIdentity() === 1 || judgeIdentity() === 2" index="/teacherInfo">教师信息</el-menu-item>
    </el-sub-menu>

    <!-- 教室管理（非下拉，直接点击进入） -->
    <el-menu-item v-if="judgeIdentity() !== 0" index="/classroomManager">
      <el-icon><OfficeBuilding /></el-icon>
      <span>教室管理</span>
    </el-menu-item>

    <!-- 信息管理子菜单（管理员/宿管可见） -->
    <el-sub-menu v-if="judgeIdentity() !== 0" index="4">
      <template #title>
        <el-icon><Message /></el-icon>
        <span>信息管理</span>
      </template>
      <el-menu-item v-if="judgeIdentity() === 2" index="/noticeInfo">公告信息</el-menu-item>
      <el-menu-item v-if="judgeIdentity() !== 0" index="/classroomRepairManage">教室设备报修管理</el-menu-item>
    </el-sub-menu>

    <!-- 申请管理子菜单（管理员/宿管可见） -->
    <el-sub-menu v-if="judgeIdentity() !== 0" index="5">
      <template #title>
        <el-icon><PieChart /></el-icon>
        <span>申请管理</span>
      </template>
      <el-menu-item v-if="judgeIdentity() !== 0" index="/classroomApproval">教室预约审批</el-menu-item>
    </el-sub-menu>

    <!-- 签到签退记录菜单（管理员/宿管可见） -->
    <el-menu-item v-if="judgeIdentity() !== 0" index="/checkInOutRecords">
      <el-icon><Clock /></el-icon>
      <span>签到签退记录</span>
    </el-menu-item>

    <!-- 学生专属菜单（仅学生可见） -->
    <el-menu-item v-if="judgeIdentity() === 0" index="/classroomRepairSubmit">
      <el-icon><SetUp /></el-icon>
      <span>教室设备报修</span>
    </el-menu-item>
    <el-menu-item v-if="judgeIdentity() === 0" index="/myClassroomRepairs">
      <el-icon><Document /></el-icon>
      <span>我的教室报修记录</span>
    </el-menu-item>
    <el-menu-item v-if="judgeIdentity() === 0" index="/classroomQuery">
      <el-icon><OfficeBuilding /></el-icon>
      <span>教室预约</span>
    </el-menu-item>
    <el-menu-item v-if="judgeIdentity() === 0" index="/myClassroomReservations">
      <el-icon><Document /></el-icon>
      <span>我的预约</span>
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
import { 
  House, User, Message, PieChart, 
  School, SetUp, Setting, OfficeBuilding,
  Clock, Document
} from '@element-plus/icons-vue';

export default {
  name: "Aside",
  components: {
    House, User, Message, PieChart,
    School, SetUp, Setting, OfficeBuilding,
    Clock, Document
  },
  data() {
    return {
      user: {},
      identity: '',
      path: this.$route.path
    };
  },
  created() {
    this.init();
  },
  methods: {
    async init() {
      try {
        const identityRes = await request.get("/main/loadIdentity");
        if (identityRes.code !== "0") {
          throw new Error("加载身份失败");
        }
        this.identity = identityRes.data;
        window.sessionStorage.setItem("identity", JSON.stringify(this.identity));

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

    judgeIdentity() {
      if (this.identity === 'stu' || this.identity === 'teacher') {
        return 0;
      } else if (this.identity === 'manager') {
        return 1;
      } else if (this.identity === 'admin') {
        return 2;
      }
      return -1;
    }
  }
};
</script>

<style scoped>
.icon {
  margin-right: 6px;
  vertical-align: middle;
}

.el-sub-menu .el-menu-item {
  height: 50px;
  line-height: 50px;
  padding: 0 45px;
  min-width: 199px;
}

.el-menu-item.is-active {
  background-color: #f0f7ff !important;
  color: #165dff !important;
}
</style>