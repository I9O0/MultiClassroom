<template>
  <div class="change-pass-container" style="padding: 20px; background: #fff; min-height: calc(100vh - 120px);">
    <h2 class="page-title" style="margin-bottom: 24px;">修改密码</h2>

    <!-- 密码修改表单：适配学生/老师，动态调用接口 -->
    <el-form
      ref="passFormRef"
      :model="passForm"
      :rules="passRules"
      label-width="120px"
      style="max-width: 600px;"
    >
      <!-- 原密码 -->
      <el-form-item label="原密码" prop="oldPass">
        <el-input
          type="password"
          v-model="passForm.oldPass"
          placeholder="请输入当前登录密码"
          autocomplete="off"
        ></el-input>
      </el-form-item>

      <!-- 新密码 -->
      <el-form-item label="新密码" prop="newPass">
        <el-input
          type="password"
          v-model="passForm.newPass"
          placeholder="请输入6-16位新密码"
          autocomplete="off"
        ></el-input>
      </el-form-item>

      <!-- 确认新密码 -->
      <el-form-item label="确认新密码" prop="confirmPass">
        <el-input
          type="password"
          v-model="passForm.confirmPass"
          placeholder="请再次输入新密码"
          autocomplete="off"
        ></el-input>
      </el-form-item>

      <!-- 提交/取消按钮 -->
      <el-form-item style="margin-top: 16px;">
        <el-button type="primary" @click="handleSubmit">提交修改</el-button>
        <el-button style="margin-left: 12px;" @click="handleCancel">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
// 引入必要依赖：请求工具、Element Plus组件和提示
import request from "@/utils/request";
import { ElMessage, ElForm, ElFormItem, ElInput, ElButton } from "element-plus";

export default {
  name: "ChangePass",
  components: {
    // 局部注册Element组件（确保表单正常显示）
    ElForm,
    ElFormItem,
    ElInput,
    ElButton
  },
  data() {
    // 确认新密码规则：与新密码一致
    const validateConfirmPass = (rule, value, callback) => {
      if (value !== this.passForm.newPass) {
        callback(new Error("两次输入的新密码不一致"));
      } else {
        callback();
      }
    };

    return {
      // 密码表单数据
      passForm: {
        oldPass: "", // 原密码
        newPass: "", // 新密码
        confirmPass: "" // 确认新密码
      },
      // 表单验证规则
      passRules: {
        oldPass: [
          { required: true, message: "请输入原密码", trigger: "blur" }
        ],
        newPass: [
          { required: true, message: "请输入新密码", trigger: "blur" },
          { min: 6, max: 16, message: "新密码长度需在6-16位之间", trigger: "blur" }
        ],
        confirmPass: [
          { required: true, message: "请确认新密码", trigger: "blur" },
          { validator: validateConfirmPass, trigger: "blur" }
        ]
      },
      identity: "" // 当前用户身份（stu/teacher，从session读取）
    };
  },
  created() {
    // 页面加载时读取身份：确保接口与身份匹配
    this.identity = JSON.parse(sessionStorage.getItem("identity"));
    // 若未获取到身份（异常情况），跳回登录页
    if (!this.identity) {
      ElMessage.warning("身份信息异常，请重新登录");
      this.$router.push("/Login");
    }
  },
  methods: {
    // 取消：返回个人信息页面
    handleCancel() {
      this.$router.push("/selfInfo");
    },

    // 提交修改：动态调用对应身份的接口
    async handleSubmit() {
      // 1. 表单验证
      const formRef = this.$refs.passFormRef;
      if (!formRef) return;

      try {
        await formRef.validate(); // 触发表单验证

        // 2. 获取当前用户用户名（学号/工号）
        const user = JSON.parse(sessionStorage.getItem("user"));
        if (!user || !user.username) {
          throw new Error("未获取到用户信息");
        }

        // 3. 动态拼接接口路径：学生→/student/updatePass，老师→/teacher/updatePass
        const apiUrl = `/${this.identity}/updatePass`;
        console.log("当前调用接口：", apiUrl); // 调试用，可删除

        // 4. 调用后端接口（参数与后端Controller一致：username/oldPass/newPass）
        const res = await request.post(apiUrl, {
          username: user.username, // 学号/工号
          password: this.passForm.oldPass, // 原密码（明文）
          phone: this.passForm.newPass // 临时用phone字段传新密码（与后端一致）
        });

        // 5. 处理接口响应
        if (res.code === "0") {
          ElMessage.success("密码修改成功！请重新登录");
          // 清除session，跳回登录页
          sessionStorage.clear();
          this.$router.push("/Login");
        } else {
          // 显示后端返回的错误（如“原密码错误”“学生不存在”）
          ElMessage.error(res.msg || "修改失败，请重试");
        }
      } catch (err) {
        // 处理表单验证失败或接口异常
        ElMessage.error(err.message || "操作异常，请重试");
      }
    }
  }
};
</script>

<style scoped>
/* 页面基础样式（可选，可根据需求调整） */
.page-title {
  font-size: 18px;
  font-weight: 500;
  color: #333;
}
</style>