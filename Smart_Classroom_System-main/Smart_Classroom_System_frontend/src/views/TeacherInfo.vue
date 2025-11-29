<template>
  <div class="app-container">
    <el-card>
      <!-- 搜索和新增按钮 -->
      <div style="margin-bottom: 15px; display: flex; justify-content: space-between;">
        <el-input
          v-model="search"
          placeholder="请输入教师姓名搜索"
          style="width: 300px"
          clearable
          @clear="reset"
        >
          <template #append>
            <el-button type="primary" @click="load">搜索</el-button>
          </template>
        </el-input>
        <el-button type="primary" @click="add">新增教师</el-button>
      </div>

      <!-- 教师列表表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        style="width: 100%"
      >
        <el-table-column prop="username" label="工号" width="120" align="center"></el-table-column>
        <el-table-column prop="name" label="姓名" width="120" align="center"></el-table-column>
        <el-table-column prop="department" label="所属部门" width="180" align="center"></el-table-column>
        <el-table-column prop="title" label="职称" width="120" align="center"></el-table-column>
        <el-table-column prop="phone" label="联系电话" width="150" align="center"></el-table-column>
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row.username)" style="margin-left: 5px">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div style="margin-top: 15px; text-align: right;">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[5, 10, 20]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
        ></el-pagination>
      </div>

      <!-- 新增/编辑弹窗 -->
      <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑教师' : '新增教师'" width="500px">
        <el-form ref="form" :model="form" :rules="rules" label-width="100px">
          <el-form-item label="工号" prop="username" :disabled="isEdit">
            <el-input v-model="form.username" placeholder="请输入工号"></el-input>
          </el-form-item>
          <el-form-item label="姓名" prop="name">
            <el-input v-model="form.name" placeholder="请输入姓名"></el-input>
          </el-form-item>
          <!-- 部门改为下拉选择 -->
          <el-form-item label="所属部门" prop="department">
            <el-select v-model="form.department" placeholder="请选择所属部门">
              <el-option 
                v-for="dept in departments" 
                :key="dept" 
                :label="dept" 
                :value="dept"
              ></el-option>
            </el-select>
          </el-form-item>
          <!-- 职称改为下拉选择 -->
          <el-form-item label="职称" prop="title">
            <el-select v-model="form.title" placeholder="请选择职称">
              <el-option 
                v-for="title in titles" 
                :key="title" 
                :label="title" 
                :value="title"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="联系电话" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入联系电话"></el-input>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="form.status" placeholder="请选择状态">
              <el-option label="正常" value="1"></el-option>
              <el-option label="禁用" value="0"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="密码" prop="password" v-if="!isEdit">
            <el-input v-model="form.password" type="password" placeholder="初始密码"></el-input>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="save">确定</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
import request from "@/utils/request";
import { ElMessage } from "element-plus";

export default {
  name: "TeacherInfo",
  data() {
    // 手机号验证规则
    const validatePhone = (rule, value, callback) => {
      const reg = /^1[3-9]\d{9}$/;
      if (!value) {
        return callback(new Error("请输入联系电话"));
      }
      if (!reg.test(value)) {
        callback(new Error("请输入正确的手机号格式"));
      } else {
        callback();
      }
    };

    return {
      loading: false,
      dialogVisible: false,
      isEdit: false, // false-新增，true-编辑
      search: "",
      currentPage: 1,
      pageSize: 10,
      total: 0,
      tableData: [],
      // 前端静态数据：部门列表
      departments: ["计算机学院", "电子信息学院", "外国语学院", "商学院", "文学院", "数学与统计学院", "物理学院", "化学学院"],
      // 前端静态数据：职称列表
      titles: ["教授", "副教授", "讲师", "助教", "研究员", "副研究员"],
      form: {
        username: "",
        name: "",
        department: "",
        title: "",
        phone: "",
        status: 1, // 默认正常
        password: ""
      },
      rules: {
        username: [
          { required: true, message: "请输入工号", trigger: "blur" },
          { pattern: /^[A-Za-z0-9]{4,10}$/, message: "工号由4-10位数字或字母组成", trigger: "blur" }
        ],
        name: [
          { required: true, message: "请输入姓名", trigger: "blur" },
          { pattern: /^[\u4e00-\u9fa5]{2,10}$/, message: "姓名为2-10位汉字", trigger: "blur" }
        ],
        department: [
          { required: true, message: "请选择所属部门", trigger: "change" }
        ],
        title: [
          { required: true, message: "请选择职称", trigger: "change" }
        ],
        phone: [
          { required: true, validator: validatePhone, trigger: "blur" }
        ],
        status: [
          { required: true, message: "请选择状态", trigger: "change" }
        ],
        password: [
          { required: true, message: "请输入初始密码", trigger: "blur" },
          { min: 6, max: 16, message: "密码长度为6-16位", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.load(); // 页面加载时查询教师列表
  },
  methods: {
    // 加载教师列表（查）
    load() {
      this.loading = true;
      request.get("/teacher/find", {
        params: {
          pageNum: this.currentPage,
          pageSize: this.pageSize,
          search: this.search
        }
      }).then(res => {
        if (res.code === "0") {
          this.tableData = res.data.records;
          this.total = res.data.total;
        } else {
          ElMessage.error(res.msg || "查询失败");
        }
        this.loading = false;
      }).catch(() => {
        this.loading = false;
        ElMessage.error("网络错误");
      });
    },

    // 新增教师（增）
    add() {
      this.isEdit = false;
      this.dialogVisible = true;
      this.$nextTick(() => {
        this.$refs.form.resetFields();
        this.form = { status: 1 }; // 重置表单，默认状态为正常
      });
    },

    // 编辑教师（改）
    handleEdit(row) {
      this.isEdit = true;
      this.dialogVisible = true;
      this.$nextTick(() => {
        this.$refs.form.resetFields();
        this.form = { ...row }; // 复制行数据到表单
      });
    },

    // 保存新增/编辑（增/改）
    save() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.isEdit) {
            // 编辑：调用更新接口
            request.put("/teacher/update", this.form).then(res => {
              if (res.code === "0") {
                ElMessage.success("更新成功");
                this.dialogVisible = false;
                this.load(); // 重新加载列表
              } else {
                ElMessage.error(res.msg || "更新失败");
              }
            });
          } else {
            // 新增：调用新增接口
            request.post("/teacher/add", this.form).then(res => {
              if (res.code === "0") {
                ElMessage.success("新增成功");
                this.dialogVisible = false;
                this.load(); // 重新加载列表
              } else {
                ElMessage.error(res.msg || "新增失败");
              }
            });
          }
        }
      });
    },

    // 删除教师（删）
    handleDelete(username) {
      this.$confirm("确定要删除该教师吗？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        request.delete(`/teacher/delete/${username}`).then(res => {
          if (res.code === "0") {
            ElMessage.success("删除成功");
            this.load(); // 重新加载列表
          } else {
            ElMessage.error(res.msg || "删除失败");
          }
        });
      }).catch(() => {
        ElMessage.info("已取消删除");
      });
    },

    // 分页相关
    handleSizeChange(size) {
      this.pageSize = size;
      this.load();
    },
    handleCurrentChange(page) {
      this.currentPage = page;
      this.load();
    },

    // 重置搜索
    reset() {
      this.search = "";
      this.currentPage = 1;
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