<template>
  <div class="app-container">
    <el-card>
      <!-- 搜索和新增按钮区域 -->
      <div style="margin-bottom: 15px; display: flex; justify-content: space-between; align-items: center;">
        <el-input
          v-model="search"
          placeholder="请输入搜索内容（姓名/学号/专业）"
          style="width: 300px"
          clearable
          @clear="load"
        >
          <template #append>
            <el-button type="primary" icon="Search" @click="load"></el-button>
          </template>
        </el-input>
        <el-button type="primary" icon="Plus" @click="add">新增学生</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <!-- 表格内容不变 -->
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column label="学号" prop="username" width="120"></el-table-column>
        <el-table-column label="姓名" prop="name" width="100"></el-table-column>
        <el-table-column label="专业" prop="major"></el-table-column>
        <el-table-column label="年级" prop="grade" width="100"></el-table-column>
        <el-table-column label="手机号" prop="phone"></el-table-column>
        <el-table-column label="状态" prop="status" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button
              type="primary"
              icon="Edit"
              size="small"
              @click="edit(scope.row)"
            ></el-button>
            <el-button
              type="danger"
              icon="Delete"
              size="small"
              @click="remove(scope.row.username)"
              style="margin-left: 5px"
            ></el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页区域 -->
      <div style="margin-top: 15px; display: flex; justify-content: flex-end;">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pageNum"
          :page-sizes="[5, 10, 20, 50]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
        ></el-pagination>
      </div>

      <!-- 新增/编辑弹窗 -->
      <el-dialog v-model="dialogVisible" :title="judgeAddOrEdit ? '新增学生' : '编辑学生'" width="50%">
        <el-form
          ref="form"
          :model="form"
          :rules="rules"
          label-width="100px"
          style="margin-top: 20px"
        >
          <el-form-item label="学号" prop="username" :disabled="!judgeAddOrEdit">
            <el-input v-model="form.username" style="width: 80%" :disabled="!judgeAddOrEdit"></el-input>
          </el-form-item>
          <el-form-item label="姓名" prop="name">
            <el-input v-model="form.name" style="width: 80%"></el-input>
          </el-form-item>
          
          <!-- 专业下拉框 -->
          <el-form-item label="专业" prop="major">
            <el-select v-model="form.major" placeholder="请选择专业" style="width: 80%">
              <el-option 
                v-for="major in majorOptions" 
                :key="major.value" 
                :label="major.label" 
                :value="major.value"
              ></el-option>
            </el-select>
          </el-form-item>
          
          <!-- 年级下拉框 -->
          <el-form-item label="年级" prop="grade">
            <el-select v-model="form.grade" placeholder="请选择年级" style="width: 80%">
              <el-option 
                v-for="grade in gradeOptions" 
                :key="grade.value" 
                :label="grade.label" 
                :value="grade.value"
              ></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" style="width: 80%"></el-input>
          </el-form-item>

          <!-- 密码区域：新增时显示密码，编辑时隐藏 -->
          <el-form-item label="密码" prop="password" v-if="judgeAddOrEdit">
            <el-input
              v-model="form.password"
              :type="showpassword ? 'password' : 'text'"
              style="width: 80%"
            >
              <template #suffix>
                <el-icon @click="showpassword = !showpassword">
                  <EyeOff v-if="showpassword" />
                  <Eye v-else />
                </el-icon>
              </template>
            </el-input>
          </el-form-item>

          <!-- 确认密码：仅新增时显示 -->
          <el-form-item label="确认密码" prop="checkPass" v-if="judgeAddOrEdit">
            <el-input
              v-model="form.checkPass"
              :type="showpassword ? 'password' : 'text'"
              style="width: 80%"
            ></el-input>
          </el-form-item>

          <el-form-item label="状态" prop="status">
            <el-switch
              v-model="form.status"
              active-value="1"
              inactive-value="0"
              active-text="正常"
              inactive-text="禁用"
            ></el-switch>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="save">确认</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
import request from "@/utils/request";
import { ElMessage, ElMessageBox } from "element-plus";
import { Eye, EyeOff, Search, Plus, Edit, Delete } from "@element-plus/icons-vue";

export default {
  name: "StuInfo",
  components: { Eye, EyeOff, Search, Plus, Edit, Delete },
  data() {
    // 手机号验证规则
    const validatePhone = (rule, value, callback) => {
      const reg = /^1[3-9]\d{9}$/;
      if (!value) {
        return callback(new Error("请输入手机号"));
      }
      if (!reg.test(value)) {
        callback(new Error("请输入正确的手机号格式"));
      } else {
        callback();
      }
    };

    // 密码一致性验证
    const validateCheckPass = (rule, value, callback) => {
      if (value !== this.form.password) {
        callback(new Error("两次输入的密码不一致"));
      } else {
        callback();
      }
    };

    return {
      loading: false,
      tableData: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
      search: "",
      dialogVisible: false,
      judgeAddOrEdit: true, // true: 新增, false: 编辑
      showpassword: true,
      // 专业选项
      majorOptions: [
        { label: '计算机科学与技术', value: '计算机科学与技术' },
        { label: '软件工程', value: '软件工程' },
        { label: '电子信息工程', value: '电子信息工程' },
        { label: '通信工程', value: '通信工程' },
        { label: '自动化', value: '自动化' }
        // 可根据实际需求添加更多专业
      ],
      // 年级选项
      gradeOptions: [
        { label: '2020级', value: '2020级' },
        { label: '2021级', value: '2021级' },
        { label: '2022级', value: '2022级' },
        { label: '2023级', value: '2023级' },
        { label: '2024级', value: '2024级' }
        // 可根据实际需求添加更多年级
      ],
      form: {
        username: "",
        name: "",
        major: "",
        grade: "",
        phone: "",
        password: "",
        checkPass: "",
        status: 1
      },
      rules: {
        username: [
          { required: true, message: "请输入学号", trigger: "blur" },
          { min: 1, max: 20, message: "学号长度不能超过20位", trigger: "blur" }
        ],
        name: [
          { required: true, message: "请输入姓名", trigger: "blur" },
          { min: 1, max: 20, message: "姓名长度不能超过20位", trigger: "blur" }
        ],
        major: [
          { required: true, message: "请选择专业", trigger: "change" }
        ],
        grade: [
          { required: true, message: "请选择年级", trigger: "change" }
        ],
        phone: [
          { required: true, validator: validatePhone, trigger: "blur" }
        ],
        password: [
          { required: true, message: "请输入密码", trigger: "blur" },
          { min: 6, max: 16, message: "密码长度为6-16位", trigger: "blur" }
        ],
        checkPass: [
          { required: true, message: "请确认密码", trigger: "blur" },
          { validator: validateCheckPass, trigger: "blur" }
        ]
      },
      multipleSelection: []
    };
  },
  created() {
    this.load();
  },
  methods: {
    // 加载表格数据（方法不变）
    load() {
      this.loading = true;
      request.get("/stu/find", {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          search: this.search
        }
      }).then(res => {
        this.loading = false;
        if (res.code === "0") {
          this.tableData = res.data.records;
          this.total = res.data.total;
        } else {
          ElMessage.error(res.msg || "加载失败");
        }
      }).catch(() => {
        this.loading = false;
        ElMessage.error("网络错误，加载失败");
      });
    },

    // 其他方法（handleSelectionChange、add、edit、remove、save、handleSizeChange、handleCurrentChange）不变
    handleSelectionChange(val) {
      this.multipleSelection = val;
    },
    add() {
      this.judgeAddOrEdit = true;
      this.dialogVisible = true;
      this.$refs.form.resetFields();
      this.form.status = 1;
    },
    edit(row) {
      this.judgeAddOrEdit = false;
      this.dialogVisible = true;
      this.$refs.form.resetFields();
      this.form = { ...row };
      this.form.password = "";
      this.form.checkPass = "";
    },
    remove(username) {
      ElMessageBox.confirm(
        `确定要删除学号为 ${username} 的学生吗？`,
        "确认删除",
        {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        }
      ).then(() => {
        request.delete(`/stu/delete/${username}`).then(res => {
          if (res.code === "0") {
            ElMessage.success("删除成功");
            this.load();
          } else {
            ElMessage.error(res.msg || "删除失败");
          }
        });
      }).catch(() => {
        ElMessage.info("已取消删除");
      });
    },
    save() {
      this.$refs.form.validate(valid => {
        if (valid) {
          const formData = { ...this.form };
          delete formData.checkPass;

          if (this.judgeAddOrEdit) {
            request.post("/stu/add", formData).then(res => {
              if (res.code === "0") {
                ElMessage.success("新增成功");
                this.dialogVisible = false;
                this.load();
              } else {
                ElMessage.error(res.msg || "新增失败");
              }
            });
          } else {
            delete formData.password;
            request.put("/stu/update", formData).then(res => {
              if (res.code === "0") {
                ElMessage.success("更新成功");
                this.dialogVisible = false;
                this.load();
              } else {
                ElMessage.error(res.msg || "更新失败");
              }
            });
          }
        }
      });
    },
    handleSizeChange(val) {
      this.pageSize = val;
      this.pageNum = 1;
      this.load();
    },
    handleCurrentChange(val) {
      this.pageNum = val;
      this.load();
    }
  }
};
</script>

<style scoped>
.app-container {
  padding: 20px;
}
</style>