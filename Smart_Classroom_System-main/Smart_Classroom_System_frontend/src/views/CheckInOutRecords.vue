<template>
  <div class="container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>签到        {{ currentUser.identity === 'manager' ? '本楼宇' : '' }}签到签退记录管理
      </h2>
      <p class="header-desc">
        {{ currentUser.identity === 'manager' ? '查看和管理本楼宇' : '查看和管理所有' }}用户的教室预约签到签退记录
      </p>
    </div>

    <!-- 搜索区域 -->
    <div class="search-card">
      <div class="search-bar">
        <el-input 
          v-model="searchForm.username" 
          placeholder="请输入用户名/学号/工号" 
          style="width: 200px; margin-right: 15px;"
          clearable
          :prefix-icon="Search"
        ></el-input>
        <el-select 
          v-model="searchForm.identity" 
          placeholder="请选择身份" 
          style="width: 150px; margin-right: 15px;"
          clearable
        >
          <el-option label="全部" value=""></el-option>
          <el-option label="学生" value="stu"></el-option>
          <el-option label="教师" value="teacher"></el-option>
        </el-select>
        <!-- 楼宇筛选 -->
        <el-select 
          v-model="searchForm.buildingId" 
          placeholder="请选择楼宇" 
          style="width: 150px; margin-right: 15px;"
          clearable
          v-if="showBuildingFilter"
        >
          <el-option label="全部" value=""></el-option>
          <el-option 
            v-for="building in buildingList" 
            :key="building.id" 
            :label="building.name" 
            :value="building.id"
          ></el-option>
        </el-select>
        <!-- 日期筛选 -->
        <el-date-picker
          v-model="searchForm.date"
          type="date"
          placeholder="选择日期"
          style="width: 200px; margin-right: 15px;"
          clearable
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          @change="handleSearch"
        ></el-date-picker>
        <!-- 查询按钮：使用稳定的Search图标，保留加载状态 -->
        <el-button 
          type="primary" 
          @click="handleSearch" 
          class="search-btn"
          :loading="isSearching"
        >
          <Search class="icon" />  <!-- 换回通用的Search图标 -->
          <span>查询</span>
        </el-button>
        <!-- 重置按钮：使用稳定的Refresh图标 -->
        <el-button @click="resetForm" class="reset-btn">
          <Refresh class="icon" />  <!-- 确保图标存在 -->
          <span>重置</span>
        </el-button>
      </div>
    </div>

    <!-- 数据统计卡片 -->
    <div class="stats-container">
      <el-card class="stat-card">
        <div class="stat-item">
          <p class="stat-label">总记录数</p>
          <p class="stat-value">{{ total }}</p>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-item">
          <p class="stat-label">未签到</p>
          <p class="stat-value uncheckin">{{ tableData.filter(item => !item.checkInTime).length }}</p>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-item">
          <p class="stat-label">已签到</p>
          <p class="stat-value checkedin">{{ tableData.filter(item => item.checkInTime && !item.checkOutTime).length }}</p>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-item">
          <p class="stat-label">已签退</p>
          <p class="stat-value checkedout">{{ tableData.filter(item => item.checkOutTime).length }}</p>
        </div>
      </el-card>
    </div>

    <!-- 表格区域 -->
    <el-card class="table-card">
      <el-table 
        :data="tableData" 
        border 
        style="width: 100%;"
        :header-cell-style="headerCellStyle"
        :row-class-name="tableRowClassName"
        stripe
        empty-text="暂无数据"
        v-loading="loading"
        element-loading-text="加载中..."
        element-loading-spinner="el-icon-loading"
        element-loading-background="rgba(255, 255, 255, 0.8)"
      >
        <el-table-column prop="username" label="用户名/学号/工号" width="150"></el-table-column>
        <el-table-column prop="reserverName" label="姓名" width="120"></el-table-column>
        <el-table-column 
          prop="identity" 
          label="身份" 
          width="100"
          :formatter="formatIdentity"
        ></el-table-column>
        <el-table-column prop="buildingId" label="楼宇" width="120"></el-table-column>
        <el-table-column prop="classroomId" label="教室号" width="120"></el-table-column>
        <el-table-column prop="reserveDate" label="预约日期" width="120"></el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="120"></el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="120"></el-table-column>
        <el-table-column 
          prop="checkStatus" 
          label="签到状态" 
          width="120"
        >
          <template #default="scope">
            <el-tag 
              :type="scope.row.checkInTime ? (scope.row.checkOutTime ? 'success' : 'warning') : 'info'"
              size="small"
              class="status-tag"
            >
              {{ formatStatus(scope.row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="checkInTime" label="签到时间" width="180"></el-table-column>
        <el-table-column prop="checkOutTime" label="签退时间" width="180"></el-table-column>
      </el-table>
    </el-card>

    <!-- 分页区域 -->
    <div class="pagination-container">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pageNum"
        :page-sizes="[5, 10, 20, 50]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        background
        class="pagination"
      ></el-pagination>
    </div>
  </div>
</template>

<script>
import request from "@/utils/request";
import { onMounted, reactive, ref, inject, computed } from "vue";
import { ElMessage } from "element-plus";
// 只导入Element Plus稳定存在的图标
import { Search, Refresh } from "@element-plus/icons-vue";

export default {
  name: "CheckInOutRecords",
  components: {
    Search,  // 注册图标组件
    Refresh  // 注册图标组件
  },
  setup() {
    // 获取全局存储的用户信息
    const userStore = inject('userStore');
    const currentUser = userStore?.userInfo || {};
    
    // 搜索表单
    const searchForm = reactive({
      username: "",
      identity: "",
      date: "",
      buildingId: ""
    });

    // 搜索按钮加载状态（确保声明并导出）
    const isSearching = ref(false);

    // 楼宇列表和显示控制
    const buildingList = ref([]);
    const showBuildingFilter = computed(() => {
      return ['admin', 'manager'].includes(currentUser.identity);
    });

    // 表格数据
    const tableData = ref([]);
    const total = ref(0);
    const pageNum = ref(1);
    const pageSize = ref(10);
    const loading = ref(false);

    // 表头样式
    const headerCellStyle = computed(() => ({
      background: '#f0f2f5',
      color: '#303133',
      fontWeight: '600',
      padding: '12px 0',
      borderBottom: '1px solid #e5e6eb'
    }));

    // 获取楼宇列表
    const getBuildingList = async () => {
      try {
        const res = await request.get("/building/listAll");
        if (res.code === "0") {
          buildingList.value = res.data;
        }
      } catch (err) {
        console.error("获取楼宇列表失败：", err);
      }
    };

    // 获取记录列表
    const getRecords = async () => {
      try {
        isSearching.value = true;  // 开始加载
        loading.value = true;
        const params = {
          pageNum: pageNum.value,
          pageSize: pageSize.value,
          username: searchForm.username,
          identity: searchForm.identity,
          date: searchForm.date,
          buildingId: searchForm.buildingId || ''
        };

        const res = await request.get("/classroom/reservation/admin/checkRecords", { params });
        
        if (res.code === "0") {
          tableData.value = res.data.records;
          total.value = res.data.total;
        } else {
          ElMessage.error(res.msg || "获取记录失败");
        }
      } catch (err) {
        ElMessage.error("获取记录失败：" + err.message);
      } finally {
        isSearching.value = false;  // 结束加载
        loading.value = false;
      }
    };

    // 页面加载时获取数据
    onMounted(() => {
      if (showBuildingFilter.value) {
        getBuildingList();
      }
      getRecords();
    });

    // 搜索处理
    const handleSearch = () => {
      pageNum.value = 1;
      getRecords();
    };

    // 重置表单
    const resetForm = () => {
      searchForm.username = "";
      searchForm.identity = "";
      searchForm.date = "";
      searchForm.buildingId = "";
      pageNum.value = 1;
      getRecords();
    };

    // 分页处理
    const handleSizeChange = (val) => {
      pageSize.value = val;
      getRecords();
    };

    const handleCurrentChange = (val) => {
      pageNum.value = val;
      getRecords();
    };

    // 格式化身份显示
    const formatIdentity = (row) => {
      return row.identity === "stu" ? "学生" : "教师";
    };

    // 格式化签到状态
    const formatStatus = (row) => {
      if (!row.checkInTime) return "未签到";
      if (row.checkInTime && !row.checkOutTime) return "已签到";
      return "已签退";
    };

    // 表格行样式
    const tableRowClassName = ({ row }) => {
      if (!row.checkInTime) {
        return 'warning-row';
      }
      return '';
    };

    // 确保所有模板中使用的变量都被导出
    return {
      searchForm,
      tableData,
      total,
      pageNum,
      pageSize,
      loading,
      isSearching,  // 关键：导出加载状态变量
      buildingList,
      showBuildingFilter,
      currentUser,
      handleSearch,
      resetForm,
      handleSizeChange,
      handleCurrentChange,
      formatIdentity,
      formatStatus,
      tableRowClassName,
      headerCellStyle,
      Search,  // 导出图标组件
      Refresh  // 导出图标组件
    };
  }
};
</script>

<style scoped>
.container {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  color: #1f2329;
  font-size: 20px;
  font-weight: 600;
}

.header-desc {
  margin: 8px 0 0;
  color: #6b7280;
  font-size: 14px;
}

.search-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  padding: 16px 20px;
  margin-bottom: 20px;
}

.search-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.search-btn, .reset-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px 16px;
}

/* 图标大小调整 */
.icon {
  width: 16px;
  height: 16px;
}

.stats-container {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.stat-card {
  flex: 1;
  min-width: 200px;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.stat-item {
  padding: 16px;
  text-align: center;
}

.stat-label {
  margin: 0 0 8px;
  color: #6b7280;
  font-size: 14px;
}

.stat-value {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f2329;
}

.uncheckin {
  color: #f59e0b;
}

.checkedin {
  color: #3b82f6;
}

.checkedout {
  color: #10b981;
}

.table-card {
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  margin-bottom: 20px;
}

::v-deep .el-table {
  border-radius: 8px 8px 0 0;
  overflow: hidden;
}

::v-deep .el-table th {
  background-color: #f0f2f5;
}

::v-deep .warning-row {
  background-color: #fff8e6 !important;
  transition: background-color 0.2s;
}

::v-deep .warning-row:hover > td {
  background-color: #fff3cd !important;
}

::v-deep .el-table__empty-text {
  color: #6b7280;
  padding: 60px 0;
  font-size: 14px;
}

.status-tag {
  padding: 2px 8px;
  border-radius: 4px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 10px 0;
}

::v-deep .el-pagination {
  margin-top: 0;
}

::v-deep .el-pagination.is-background .el-pager li:not(.disabled).active {
  background-color: #409eff;
  color: #fff;
}

@media (max-width: 768px) {
  .container {
    padding: 16px;
  }
  
  .search-bar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-bar .el-input,
  .search-bar .el-select,
  .search-bar .el-date-picker {
    width: 100% !important;
    margin-right: 0 !important;
    margin-bottom: 10px;
  }
  
  .stats-container {
    flex-direction: column;
  }
  
  .stat-card {
    min-width: auto;
  }
}
</style>