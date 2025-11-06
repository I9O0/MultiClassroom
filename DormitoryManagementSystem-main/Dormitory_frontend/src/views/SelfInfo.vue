<template>
  <div class="app-container">
    <el-card>
      <div style="text-align: center; margin-top: 30px">
        <!-- 头像部分 -->
        <div class="AvatarDiv">
          <el-avatar :src="image" shape="circle" size="large">
            {{ form.name && form.name.substring(0, 1) }}
          </el-avatar>
          <div class="editImg" @click="dialogVisible = true">更换头像</div>
        </div>

        <!-- 个人信息展示 -->
        <el-descriptions title="个人信息" column="1" style="margin-top: 30px">
          <el-descriptions-item :label="identity === 'stu' ? '学号' : '工号'">
            {{ form.username || '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item label="姓名">{{ form.name || '未填写' }}</el-descriptions-item>
          
          <!-- 学生特有字段 -->
          <el-descriptions-item v-if="identity === 'stu'" label="专业">{{ form.major || '未填写' }}</el-descriptions-item>
          <el-descriptions-item v-if="identity === 'stu'" label="年级">{{ form.grade || '未填写' }}</el-descriptions-item>
          
          <!-- 老师特有字段 -->
          <el-descriptions-item v-if="identity === 'teacher'" label="所属部门">{{ form.department || '未填写' }}</el-descriptions-item>
          <el-descriptions-item v-if="identity === 'teacher'" label="职称">{{ form.title || '未填写' }}</el-descriptions-item>
          
          <el-descriptions-item label="手机号">{{ form.phone || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="账号状态">
            <el-tag :type="form.status === 1 ? 'success' : 'danger'">
              {{ form.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 操作按钮：编辑信息 + 修改密码 -->
        <div style="margin-top: 30px">
          <el-button type="primary" @click="openEditDialog">编辑信息</el-button>
          <el-button type="warning" @click="openPasswordDialog">修改密码</el-button>
        </div>
      </div>

      <!-- 1. 编辑信息弹窗 -->
      <el-dialog v-model="dialogVisible" title="编辑信息" width="50%">
        <el-form
          ref="editFormRef"
          :model="form"
          :rules="editRules"
          label-width="100px"
          style="margin-top: 20px"
        >
          <el-form-item label="姓名" prop="name">
            <el-input v-model="form.name" style="width: 80%"></el-input>
          </el-form-item>
          <el-form-item v-if="identity === 'stu'" label="专业" prop="major">
            <el-input v-model="form.major" style="width: 80%"></el-input>
          </el-form-item>
          <el-form-item v-if="identity === 'stu'" label="年级" prop="grade">
            <el-input v-model="form.grade" style="width: 80%"></el-input>
          </el-form-item>
          <el-form-item v-if="identity === 'teacher'" label="所属部门" prop="department">
            <el-input v-model="form.department" style="width: 80%"></el-input>
          </el-form-item>
          <el-form-item v-if="identity === 'teacher'" label="职称" prop="title">
            <el-input v-model="form.title" style="width: 80%"></el-input>
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" style="width: 80%"></el-input>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitEdit">确定</el-button>
          </span>
        </template>
      </el-dialog>

      <!-- 2. 修改密码弹窗 -->
      <el-dialog v-model="passwordDialogVisible" title="修改密码" width="50%">
        <el-form
          ref="passwordFormRef"
          :model="passwordForm"
          :rules="passwordRules"
          label-width="100px"
          style="margin-top: 20px"
        >
          <el-form-item label="原密码" prop="oldPass">
            <el-input type="password" v-model="passwordForm.oldPass" style="width: 80%"></el-input>
          </el-form-item>
          <el-form-item label="新密码" prop="newPass">
            <el-input type="password" v-model="passwordForm.newPass" style="width: 80%"></el-input>
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPass">
            <el-input type="password" v-model="passwordForm.confirmPass" style="width: 80%"></el-input>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="passwordDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitPassword">确定</el-button>
          </span>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
import request from "@/utils/request";
import { ElMessage } from "element-plus";

export default {
  name: "SelfInfo",
  data() {
    // 手机号验证规则
    const checkPhone = (rule, value, callback) => {
      const phoneReg = /^1[3-9]\d{9}$/;
      if (!value) {
        return callback(new Error("电话号码不能为空"));
      }
      if (phoneReg.test(value)) {
        callback();
      } else {
        callback(new Error("电话号码格式不正确"));
      }
    };

    // 密码确认验证规则
    const validateConfirmPass = (rule, value, callback) => {
      if (value !== this.passwordForm.newPass) {
        callback(new Error("两次输入的新密码不一致"));
      } else {
        callback();
      }
    };

    return {
      image: "",
      identity: "",
      form: {
        username: "",
        name: "",
        phone: "",
        status: 1,
        avatar: "",
        // 学生特有
        major: "",
        grade: "",
        // 老师特有
        department: "",
        title: ""
      },

      // 编辑信息弹窗相关
      dialogVisible: false,
      editRules: {
        name: [
          { required: true, message: "请输入姓名", trigger: "blur" },
          { pattern: /^(?:[\u4E00-\u9FA5·]{2,10})$/, message: "必须由2到10个汉字组成", trigger: "blur" }
        ],
        phone: [{ required: true, validator: checkPhone, trigger: "blur" }],
        major: [{ required: true, message: "请输入专业", trigger: "blur" }],
        grade: [
          { required: true, message: "请输入年级", trigger: "blur" },
          { pattern: /^\d{4}级$/, message: "格式应为XXXX级（如2023级）", trigger: "blur" }
        ],
        department: [{ required: true, message: "请输入所属部门", trigger: "blur" }],
        title: [{ required: true, message: "请输入职称", trigger: "blur" }]
      },

      // 修改密码弹窗相关
      passwordDialogVisible: false,
      passwordForm: {
        oldPass: "",
        newPass: "",
        confirmPass: ""
      },
      passwordRules: {
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
      }
    };
  },
  created() {
    this.loadUserInfo();
    this.initAvatar();
  },
  methods: {
    // 加载用户信息
    loadUserInfo() {
      const user = JSON.parse(sessionStorage.getItem("user"));
      if (!user) {
        ElMessage.warning("未获取到用户信息，请重新登录");
        this.$router.push("/login");
        return;
      }
      this.identity = JSON.parse(sessionStorage.getItem("identity"));
      this.form = { ...user };
    },

    // 初始化头像
    initAvatar() {
      if (this.form.avatar) {
        request.get("/files/initAvatar/" + this.form.avatar).then((res) => {
          if (res.code === "0") {
            this.image = res.data.data;
          }
        });
      }
    },

    // 打开编辑信息弹窗
    openEditDialog() {
      this.dialogVisible = true;
    },

    // 提交信息编辑
    submitEdit() {
      this.$refs.editFormRef.validate(async (valid) => {
        if (valid) {
          const submitData = { ...this.form };
          // 移除非当前身份的字段
          if (this.identity === "stu") {
            delete submitData.department;
            delete submitData.title;
          }
          if (this.identity === "teacher") {
            delete submitData.major;
            delete submitData.grade;
          }

          const res = await request.put(`/${this.identity}/update`, submitData);
          if (res.code === "0") {
            ElMessage.success("信息修改成功");
            sessionStorage.setItem("user", JSON.stringify(submitData));
            this.dialogVisible = false;
          } else {
            ElMessage.error(res.msg || "修改失败");
          }
        }
      });
    },

    // 打开密码弹窗
    openPasswordDialog() {
      this.passwordDialogVisible = true;
      this.$nextTick(() => {
        this.$refs.passwordFormRef?.resetFields();
      });
    },

    // 提交密码修改（关键：参数名与后端一致）
    submitPassword() {
      this.$refs.passwordFormRef.validate(async (valid) => {
        if (valid) {
          const user = JSON.parse(sessionStorage.getItem("user"));
          try {
            // 前端传递 oldPass（原密码）、newPass（新密码），与后端参数名对齐
            const res = await request.post(`/${this.identity}/updatePass`, {
              username: user.username,   // 学号/工号
              oldPass: this.passwordForm.oldPass, // 原密码（参数名与后端接收字段一致）
              newPass: this.passwordForm.newPass  // 新密码（参数名与后端接收字段一致）
            });

            if (res.code === "0") {
              ElMessage.success("密码修改成功！请重新登录");
              this.passwordDialogVisible = false;
              sessionStorage.clear();
              this.$router.push("/Login");
            } else {
              ElMessage.error(res.msg || "修改失败");
            }
          } catch (err) {
            ElMessage.error("操作异常，请重试");
          }
        }
      });
    }
  }
};
</script>

<style scoped>
.AvatarDiv {
  position: relative;
  width: 100px;
  margin: 0 auto;
}
.editImg {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #409eff;
  color: white;
  text-align: center;
  line-height: 30px;
  font-size: 12px;
  cursor: pointer;
}
</style>