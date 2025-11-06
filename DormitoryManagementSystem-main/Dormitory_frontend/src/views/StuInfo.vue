<template>
  <div class="app-container">
    <el-card>
      <div style="margin-bottom: 15px">
        <el-input
          v-model="search"
          placeholder="请输入搜索内容"
          style="width: 300px"
          clearable
        >
          <template #append>
            <el-button type="primary" icon="Search" @click="load">搜索</el-button>
          </template>
        </el-input>
        <el-button type="success" icon="Plus" @click="add">新增</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column label="学号" prop="username" sortable></el-table-column>
        <el-table-column label="姓名" prop="name"></el-table-column>
        <el-table-column label="专业" prop="major"></el-table-column>
        <el-table-column label="年级" prop="grade"></el-table-column>
        <el-table-column label="手机号" prop="phoneNum"></el-table-column>
        <el-table-column label="操作" align="center">
          <template #default="scope">
            <el-button
              type="primary"
              icon="Edit"
              size="small"
              @click="handleEdit(scope.row)"
            ></el-button>
            <el-button
              type="danger"
              icon="Delete"
              size="small"
              @click="handleDelete(scope.row.username)"
            ></el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[5, 10, 20]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        style="margin-top: 15px"
      ></el-pagination>

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
            <el-input v-model="form.username" style="width: 80%"></el-input>
          </el-form-item>
          <el-form-item label="姓名" prop="name">
            <el-input v-model="form.name" style="width: 80%"></el-input>
          </el-form-item>
          <el-form-item label="专业" prop="major">
            <el-input v-model="form.major" style="width: 80%"></el-input>
          </el-form-item>
          <el-form-item label="年级" prop="grade">
            <el-input v-model="form.grade" style="width: 80%"></el-input>
          </el-form-item>
          <el-form-item label="手机号" prop="phoneNum">
            <el-input v-model="form.phoneNum" style="width: 80%"></el-input>
          </el-form-item>
          <el-form-item label="密码" prop="password">
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
          <el-form-item label="确认密码" prop="checkPass" v-show="!judgeAddOrEdit">
            <el-input
              v-model="form.checkPass"
              :type="showpassword ? 'password' : 'text'"
              style="width: 80%"
            ></el-input>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="cancel">取消</el-button>
            <el-button type="primary" @click="save">确定</el-button>
          </span>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script src="@/assets/js/StuInfo.js"></script>
<style scoped>
.app-container {
  padding: 20px;
}
</style>