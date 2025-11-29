<template>
  <div class="app-container">
    <el-card>
      <div style="text-align: center; margin-top: 30px">
        <!-- 个人信息展示 -->
        <el-descriptions title="个人信息" column="1" style="margin-top: 30px">
          <el-descriptions-item :label="getIdentityLabel">
            {{ form.username || '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item label="姓名：">{{ form.name || '未填写' }}</el-descriptions-item>
          
          <!-- 管理员和宿管共有字段（无宿舍相关） -->
          <el-descriptions-item v-if="['admin', 'manager'].includes(identity)" label="性别：">{{ form.gender || '未填写' }}</el-descriptions-item>
          <el-descriptions-item v-if="['admin', 'manager'].includes(identity)" label="年龄：">{{ form.age || '未填写' }}</el-descriptions-item>
          <el-descriptions-item v-if="['admin', 'manager'].includes(identity)" label="邮箱：">{{ form.email || '未填写' }}</el-descriptions-item>
          
          <!-- 学生特有字段 -->
          <el-descriptions-item v-if="identity === 'stu'" label="专业：">{{ form.major || '未填写' }}</el-descriptions-item>
          <el-descriptions-item v-if="identity === 'stu'" label="年级：">{{ form.grade || '未填写' }}</el-descriptions-item>
          
          <!-- 老师特有字段 -->
          <el-descriptions-item v-if="identity === 'teacher'" label="所属部门：">{{ form.department || '未填写' }}</el-descriptions-item>
          <el-descriptions-item v-if="identity === 'teacher'" label="职称：">{{ form.title || '未填写' }}</el-descriptions-item>
          
          <!-- 手机号适配（管理员/宿管用phoneNum，其他用phone） -->
          <el-descriptions-item label="手机号：">
            {{ form.phoneNum || form.phone || '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item label="账号状态：">
            <el-tag :type="form.status === 1 ? 'success' : 'danger'">
              {{ form.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 操作按钮 -->
        <div style="margin-top: 30px">
          <el-button type="primary" @click="dialogVisible = true">编辑信息</el-button>
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
          
          <!-- 管理员和宿管共有编辑字段（无宿舍相关） -->
          <el-form-item v-if="['admin', 'manager'].includes(identity)" label="性别" prop="gender">
            <el-select v-model="form.gender" style="width: 80%" placeholder="请选择性别">
              <el-option label="男" value="男"></el-option>
              <el-option label="女" value="女"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item v-if="['admin', 'manager'].includes(identity)" label="年龄" prop="age">
            <el-input v-model.number="form.age" type="number" min="18" max="100" style="width: 80%" placeholder="请输入年龄"></el-input>
          </el-form-item>
          <el-form-item v-if="['admin', 'manager'].includes(identity)" label="邮箱" prop="email">
            <el-input v-model="form.email" style="width: 80%" placeholder="选填，如：xxx@xxx.com"></el-input>
          </el-form-item>

          <!-- 学生特有编辑字段 -->
          <el-form-item v-if="identity === 'stu'" label="专业" prop="major">
            <el-input v-model="form.major" style="width: 80%"></el-input>
          </el-form-item>
          <el-form-item v-if="identity === 'stu'" label="年级" prop="grade">
            <el-input v-model="form.grade" style="width: 80%"></el-input>
          </el-form-item>
          
          <!-- 老师特有编辑字段 -->
          <el-form-item v-if="identity === 'teacher'" label="所属部门" prop="department">
            <el-input v-model="form.department" style="width: 80%"></el-input>
          </el-form-item>
          <el-form-item v-if="identity === 'teacher'" label="职称" prop="title">
            <el-input v-model="form.title" style="width: 80%"></el-input>
          </el-form-item>
          
          <!-- 手机号字段适配 -->
          <el-form-item label="手机号" prop="phoneNum" v-if="['admin', 'manager'].includes(identity)">
            <el-input v-model="form.phoneNum" style="width: 80%"></el-input>
          </el-form-item>
          <el-form-item label="手机号" prop="phone" v-else>
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
      identity: "",
      dialogVisible: false,
      passwordDialogVisible: false,
      
      form: {
        username: "",
        name: "",
        status: 1, // 数据中状态字段均为1（正常）
        // 管理员和宿管共有字段（无宿舍相关）
        gender: "",
        age: null,
        email: "", // 可为空
        phoneNum: "",
        // 学生特有字段
        major: "",
        grade: "",
        phone: "",
        // 老师特有字段
        department: "",
        title: ""
      },

      editRules: {
        name: [
          { required: true, message: "请输入姓名", trigger: "blur" },
          { pattern: /^(?:[\u4E00-\u9FA5·]{2,10})$/, message: "必须由2到10个汉字组成", trigger: "blur" }
        ],
        phoneNum: [{ required: true, validator: checkPhone, trigger: "blur" }],
        phone: [{ required: true, validator: checkPhone, trigger: "blur" }],
        gender: [{ required: true, message: "请选择性别", trigger: "change" }],
        age: [
          { required: true, message: "请输入年龄", trigger: "blur" },
          { type: "number", min: 18, max: 100, message: "年龄需在18-100之间", trigger: "blur" }
        ],
        email: [
          { type: "email", message: "邮箱格式不正确", trigger: "blur" }, // 仅验证格式，非必填
          { max: 50, message: "邮箱长度不能超过50个字符", trigger: "blur" }
        ],
        // 学生字段验证
        major: [{ required: true, message: "请输入专业", trigger: "blur" }],
        grade: [
          { required: true, message: "请输入年级", trigger: "blur" },
          { pattern: /^\d{4}级$/, message: "格式应为XXXX级（如2023级）", trigger: "blur" }
        ],
        // 老师字段验证
        department: [{ required: true, message: "请输入所属部门", trigger: "blur" }],
        title: [{ required: true, message: "请输入职称", trigger: "blur" }]
      },

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
  computed: {
    // 动态生成身份标签
    getIdentityLabel() {
      const labels = {
        stu: '学号：',
        teacher: '工号：',
        admin: '管理员账号：',
        manager: '教室管理员账号：'
      };
      return labels[this.identity] || '账号：';
    }
  },
  created() {
    this.loadUserInfo();
  },
  methods: {
    // 加载用户信息（适配最新manager数据字段）
    loadUserInfo() {
      const user = JSON.parse(sessionStorage.getItem("user"));
      if (!user) {
        ElMessage.warning("未获取到用户信息，请重新登录");
        this.$router.push("/login");
        return;
      }
      this.identity = JSON.parse(sessionStorage.getItem("identity"));
      // 过滤无关字段，仅保留需要的核心字段
      const { avatar, dormBuildId, password, ...userInfo } = user;
      this.form = { ...userInfo, status: user.status || 1 }; // 状态默认1（正常）
    },

    // 提交信息编辑
    submitEdit() {
      this.$refs.editFormRef.validate(async (valid) => {
        if (valid) {
          const res = await request.put(`/${this.identity}/update`, this.form);
          if (res.code === "0") {
            ElMessage.success("信息修改成功");
            sessionStorage.setItem("user", JSON.stringify(this.form));
            this.dialogVisible = false;
          } else {
            ElMessage.error(res.msg || "修改失败");
          }
        }
      });
    },

    // 密码修改相关
    openPasswordDialog() {
      this.passwordDialogVisible = true;
      this.$nextTick(() => {
        this.$refs.passwordFormRef?.resetFields();
      });
    },
    submitPassword() {
      this.$refs.passwordFormRef.validate(async (valid) => {
        if (valid) {
          const user = JSON.parse(sessionStorage.getItem("user"));
          try {
            const res = await request.post(`/${this.identity}/updatePass`, {
              username: user.username,
              oldPass: this.passwordForm.oldPass,
              newPass: this.passwordForm.newPass
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
</style>