<template>
  <div class="checkin-page">
    <!-- 页面头部 -->
    <header class="page-header">
      <div class="container">
        <h1 class="page-title">我的教室预约</h1>
        <p class="page-desc">查看已通过的预约，完成签到与签退操作</p>
      </div>
    </header>

    <!-- 筛选区域 -->
    <section class="filter-section container">
      <el-card shadow="hover" class="filter-card">
        <el-form :inline="true" :model="filterForm" class="filter-form" @submit.prevent="handleFilter">
          <el-form-item label="签到状态">
            <el-select 
              v-model="filterForm.checkStatus" 
              placeholder="全部状态" 
              clearable
              class="filter-select"
              size="medium"
            >
              <el-option label="未签到" value="0"></el-option>
              <el-option label="已签到" value="1"></el-option>
              <el-option label="已签退" value="2"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="教学楼">
            <el-select 
              v-model="filterForm.buildingId" 
              placeholder="全部教学楼" 
              clearable
              class="filter-select"
              size="medium"
            >
              <el-option 
                v-for="building in buildingList" 
                :key="building.buildingId" 
                :label="building.buildingName" 
                :value="building.buildingId"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" class="filter-btn" @click="handleFilter">
              筛选
            </el-button>
            <el-button icon="el-icon-refresh" class="reset-btn" @click="resetFilter">
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </section>

    <!-- 数据统计卡片 -->
    <section class="stats-section container">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card stat-pending">
            <div class="stat-card-header">
              <i class="el-icon-calendar stat-icon"></i>
              <h3 class="stat-title">待签到</h3>
            </div>
            <div class="stat-value">{{ pendingCheckinCount }}</div>
            <div class="stat-desc">需在预约开始前后30分钟内签到</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card stat-checked-in">
            <div class="stat-card-header">
              <i class="el-icon-check stat-icon"></i>
              <h3 class="stat-title">已签到</h3>
            </div>
            <div class="stat-value">{{ checkedInCount }}</div>
            <div class="stat-desc">需在预约结束前后30分钟内签退</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card stat-completed">
            <div class="stat-card-header">
              <i class="el-icon-circle-check stat-icon"></i>
              <h3 class="stat-title">已完成</h3>
            </div>
            <div class="stat-value">{{ completedCount }}</div>
            <div class="stat-desc">本周已完成的有效预约</div>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <!-- 预约列表 -->
    <section class="reservation-list-section container">
      <el-card shadow="hover" class="reservation-card">
        <div class="card-header">
          <h2 class="card-title">预约列表</h2>
          <el-tag type="info" class="count-tag">{{ total }} 条预约记录</el-tag>
        </div>
        <el-table 
          :data="paginationList" 
          border 
          style="width: 100%"
          v-loading="loading"
          :header-cell-style="{ 'background-color': '#f8f9fa', 'font-weight': 'bold', 'color': '#333' }"
          :row-class-name="tableRowClassName"
          empty-text="暂无符合条件的预约记录"
          :row-hover="true"
        >
          <el-table-column prop="classroomId" label="教室号" width="100" align="center"></el-table-column>
          <el-table-column prop="buildingId" label="教学楼" width="120" align="center">
            <template #default="scope">
              <el-tag type="primary" size="small">{{ getBuildingName(scope.row.buildingId) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="预约日期" width="120" align="center">
            <template #default="scope">
              {{ formatNormalDate(scope.row.reserveDate) }}
            </template>
          </el-table-column>
          <el-table-column label="预约时段" width="180" align="center">
            <template #default="scope">
              {{ scope.row.startTime }} - {{ scope.row.endTime }}
            </template>
          </el-table-column>
          <el-table-column prop="reserveReason" label="预约理由" min-width="200"></el-table-column>
          <el-table-column prop="checkStatus" label="签到状态" width="120" align="center">
            <template #default="scope">
              <el-tag :type="getCheckStatusTagType(scope.row.checkStatus)">
                {{ getCheckStatusText(scope.row.checkStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作时间" width="180" align="center">
            <template #default="scope">
              <span v-if="scope.row.checkStatus === 0">-</span>
              <span v-else-if="scope.row.checkStatus === 1">{{ formatNormalDate(scope.row.checkinTime) }}</span>
              <span v-else>{{ formatNormalDate(scope.row.checkoutTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" align="center">
            <template #default="scope">
              <!-- 签到按钮 -->
              <el-button 
                type="success" 
                size="small" 
                @click="handleCheckin(scope.row)"
                v-if="scope.row.checkStatus === 0"
                class="operation-btn checkin-btn"
              >
                <i class="el-icon-upload2"></i> 签到
              </el-button>

              <!-- 签退按钮 -->
              <el-button 
                type="primary" 
                size="small" 
                @click="handleCheckout(scope.row)"
                v-if="scope.row.checkStatus === 1"
                class="operation-btn checkout-btn"
              >
                <i class="el-icon-download"></i> 签退
              </el-button>

              <!-- 已完成/不可操作 -->
              <el-button 
                type="text" 
                size="small" 
                disabled
                v-if="scope.row.checkStatus === 2"
                class="completed-btn"
              >
                <i class="el-icon-success"></i> 已完成
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="currentPage"
            :page-sizes="[10, 20, 50, 100]"
            :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            background
            v-if="total > 0"
          ></el-pagination>
        </div>
      </el-card>
    </section>

    <!-- 操作成功弹窗 -->
    <el-dialog 
      v-model="successDialogVisible" 
      title="操作成功" 
      width="300px"
      center
      :show-close="false"
      class="success-dialog"
    >
      <div class="dialog-content">
        <i class="el-icon-success-circle success-icon"></i>
        <p class="success-message">{{ successMessage }}</p>
      </div>
      <template #footer>
        <el-button type="primary" @click="successDialogVisible = false">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import request from "@/utils/request";
import { ElMessage } from "element-plus";

export default {
  name: "MyClassroomReservations",
  data() {
    return {
      // 筛选表单
      filterForm: {
        checkStatus: "",
        buildingId: ""
      },
      // 分页参数
      currentPage: 1,
      pageSize: 10,
      total: 0,
      loading: false,
      // 数据列表
      reservationList: [],
      paginationList: [],
      buildingList: [],
      // 统计数据
      pendingCheckinCount: 0,
      checkedInCount: 0,
      completedCount: 0,
      // 弹窗状态
      successDialogVisible: false,
      successMessage: "",
      // 用户身份
      identity: ""
    };
  },
  created() {
    // 初始化用户身份
    this.identity = JSON.parse(sessionStorage.getItem("identity")) || "stu";
    // 加载数据
    this.getBuildingList();
    this.getReservationList();
  },
  methods: {
    // 获取教学楼列表
    async getBuildingList() {
      try {
        const res = await request.get("/building/listAll");
        if (res.code === "0") {
          const filterRule = this.identity === "stu" ? ["J1", "J2"] : ["J3", "J4"];
          this.buildingList = res.data.filter(b => filterRule.includes(b.buildingId));
        }
      } catch (err) {
        ElMessage.error("获取教学楼列表失败：" + err.message);
      }
    },

    // 获取预约列表
    async getReservationList() {
      this.loading = true;
      try {
        const user = JSON.parse(sessionStorage.getItem("user"));
        if (!user?.username) {
          ElMessage.warning("请先登录");
          this.$router.push("/Login");
          return;
        }

        // 构造请求参数
        const params = {
          username: user.username.trim(),
          pageNum: this.currentPage,
          pageSize: this.pageSize
        };

        // 追加筛选参数
        if (this.filterForm.checkStatus) params.checkStatus = this.filterForm.checkStatus;
        if (this.filterForm.buildingId) params.buildingId = this.filterForm.buildingId;

        const res = await request.get("/classroom/reservation/audited", { params });
        if (res.code === "0") {
          this.reservationList = res.data.records || [];
          this.total = res.data.total || 0;
          this.paginationList = this.reservationList;
          this.calcStatistics();
        } else {
          ElMessage.error(res.msg || "查询预约失败");
        }
      } catch (err) {
        ElMessage.error("加载预约列表失败：" + err.message);
      } finally {
        this.loading = false;
      }
    },

    // 筛选处理
    handleFilter() {
      this.currentPage = 1;
      this.getReservationList();
    },

    // 重置筛选
    resetFilter() {
      this.filterForm = { checkStatus: "", buildingId: "" };
      this.currentPage = 1;
      this.getReservationList();
      ElMessage.info("筛选条件已重置");
    },

    // 统计计算
    calcStatistics() {
      this.pendingCheckinCount = this.reservationList.filter(item => item.checkStatus === 0).length;
      this.checkedInCount = this.reservationList.filter(item => item.checkStatus === 1).length;
      this.completedCount = this.reservationList.filter(item => item.checkStatus === 2).length;
    },

    // 签到状态文本
    getCheckStatusText(status) {
      const statusMap = { "0": "未签到", "1": "已签到", "2": "已签退" };
      return statusMap[String(status)] || "未知";
    },

    // 签到状态标签样式
    getCheckStatusTagType(status) {
      const typeMap = { "0": "warning", "1": "info", "2": "success" };
      return typeMap[String(status)] || "info";
    },

    // 获取教学楼名称
    getBuildingName(buildingId) {
      const building = this.buildingList.find(item => item.buildingId === buildingId);
      return building?.buildingName || buildingId;
    },

    // 日期格式化方法
    formatNormalDate(dateStr) {
      if (!dateStr) return "";
      try {
        // 分割字符串，提取前3部分
        const parts = dateStr.split(/[-/]/).slice(0, 3);
        
        // 补全必要的日期部分
        while (parts.length < 3) {
          parts.push('01');
        }
        
        // 构建标准日期字符串
        const pureDate = parts.join('-');
        const date = new Date(pureDate);
        
        // 验证日期有效性
        if (isNaN(date.getTime())) {
          return pureDate;
        }
        
        // 格式化输出为 yyyy-MM-dd
        return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`;
      } catch (err) {
        return dateStr.split(/[-/]/).slice(0, 3).join('-');
      }
    },

    // 判断是否可签到（仅做逻辑判断）
    canCheckin(reservation) {
      if (reservation.checkStatus !== 0) return false;
      
      const now = new Date();
      const [year, month, day] = reservation.reserveDate.split(/[-/]/).slice(0, 3);
      const [startHour, startMinute] = reservation.startTime.split(":");
      const startTime = new Date(year, month - 1, day, startHour, startMinute);
      
      const checkinStart = new Date(startTime.getTime() - 30 * 60 * 1000);
      const checkinEnd = new Date(startTime.getTime() + 30 * 60 * 1000);
      
      return now >= checkinStart && now <= checkinEnd;
    },

    // 判断是否可签退（仅做逻辑判断）
    canCheckout(reservation) {
      if (reservation.checkStatus !== 1) return false;
      
      const now = new Date();
      const [year, month, day] = reservation.reserveDate.split(/[-/]/).slice(0, 3);
      const [endHour, endMinute] = reservation.endTime.split(":");
      const endTime = new Date(year, month - 1, day, endHour, endMinute);
      
      const checkoutStart = new Date(endTime.getTime() - 30 * 60 * 1000);
      const checkoutEnd = new Date(endTime.getTime() + 30 * 60 * 1000);
      
      return now >= checkoutStart && now <= checkoutEnd;
    },

    // 处理签到（带完整提示）
    async handleCheckin(reservation) {
      // 先校验是否可签到
      const canCheck = this.canCheckin(reservation);
      if (!canCheck) {
        const now = new Date();
        const [year, month, day] = reservation.reserveDate.split(/[-/]/).slice(0, 3);
        const [startHour, startMinute] = reservation.startTime.split(":");
        const startTime = new Date(year, month - 1, day, startHour, startMinute);
        const checkinStart = new Date(startTime.getTime() - 30 * 60 * 1000);
        const checkinEnd = new Date(startTime.getTime() + 30 * 60 * 1000);

        if (now < checkinStart) {
          const startStr = `${checkinStart.getHours().toString().padStart(2, '0')}:${checkinStart.getMinutes().toString().padStart(2, '0')}`;
          ElMessage.info(`签到未开始，可在 ${startStr} 后进行`);
        } else if (now > checkinEnd) {
          const endStr = `${checkinEnd.getHours().toString().padStart(2, '0')}:${checkinEnd.getMinutes().toString().padStart(2, '0')}`;
          ElMessage.warning(`签到已结束，签到时段为 ${endStr} 前`);
        } else {
          ElMessage.error("当前无法签到，请稍后重试");
        }
        return;
      }

      // 校验通过，执行签到请求
      try {
        const user = JSON.parse(sessionStorage.getItem("user"));
        if (!user?.username) {
          ElMessage.warning("请先登录");
          this.$router.push("/Login");
          return;
        }

        const res = await request.post(`/classroom/reservation/checkIn/${reservation.id}`, null, {
          params: { username: user.username }
        });

        if (res.code === "0") {
          this.successMessage = "签到成功！";
          this.successDialogVisible = true;
          this.getReservationList(); // 刷新列表
        } else {
          ElMessage.error(res.msg || "签到失败，请重试");
        }
      } catch (err) {
        ElMessage.error("签到操作失败：" + err.message);
      }
    },

    // 处理签退（带完整提示）
    async handleCheckout(reservation) {
      // 先校验是否可签退
      const canCheck = this.canCheckout(reservation);
      if (!canCheck) {
        const now = new Date();
        const [year, month, day] = reservation.reserveDate.split(/[-/]/).slice(0, 3);
        const [endHour, endMinute] = reservation.endTime.split(":");
        const endTime = new Date(year, month - 1, day, endHour, endMinute);
        const checkoutStart = new Date(endTime.getTime() - 30 * 60 * 1000);
        const checkoutEnd = new Date(endTime.getTime() + 30 * 60 * 1000);

        if (now < checkoutStart) {
          const startStr = `${checkoutStart.getHours().toString().padStart(2, '0')}:${checkoutStart.getMinutes().toString().padStart(2, '0')}`;
          ElMessage.info(`签退未开始，可在 ${startStr} 后进行`);
        } else if (now > checkoutEnd) {
          const endStr = `${checkoutEnd.getHours().toString().padStart(2, '0')}:${checkoutEnd.getMinutes().toString().padStart(2, '0')}`;
          ElMessage.warning(`签退已结束，签退时段为 ${endStr} 前`);
        } else {
          ElMessage.error("当前无法签退，请稍后重试");
        }
        return;
      }

      // 校验通过，执行签退请求
      try {
        const user = JSON.parse(sessionStorage.getItem("user"));
        if (!user?.username) {
          ElMessage.warning("请先登录");
          this.$router.push("/Login");
          return;
        }

        const res = await request.post(`/classroom/reservation/checkOut/${reservation.id}`, null, {
          params: { username: user.username }
        });

        if (res.code === "0") {
          this.successMessage = "签退成功！";
          this.successDialogVisible = true;
          this.getReservationList(); // 刷新列表
        } else {
          ElMessage.error(res.msg || "签退失败，请重试");
        }
      } catch (err) {
        ElMessage.error("签退操作失败：" + err.message);
      }
    },

    // 表格行样式
    tableRowClassName({ row }) {
      if (row.checkStatus === 0) return "row-pending";
      if (row.checkStatus === 1) return "row-checked-in";
      return "row-completed";
    },

    // 分页大小改变
    handleSizeChange(val) {
      this.pageSize = val;
      this.getReservationList();
    },

    // 当前页改变
    handleCurrentChange(val) {
      this.currentPage = val;
      this.getReservationList();
    }
  }
};
</script>

<style scoped>
.checkin-page {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
  transition: all 0.3s ease;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 15px;
}

.page-header {
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eaecef;
}

.page-title {
  font-size: 28px;
  color: #333;
  margin-bottom: 8px;
  font-weight: 600;
}

.page-desc {
  font-size: 14px;
  color: #666;
  opacity: 0.9;
}

.filter-section {
  margin-bottom: 25px;
}

.filter-card {
  background-color: #fff;
  border-radius: 8px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.filter-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.filter-form {
  padding: 15px 0;
}

.filter-select {
  width: 180px;
  margin-right: 10px;
}

.filter-btn {
  margin-right: 10px;
}

.reset-btn {
  background-color: #f5f7fa;
  color: #666;
  border-color: #ddd;
}

.reset-btn:hover {
  background-color: #e9ecef;
  color: #333;
}

.stats-section {
  margin-bottom: 25px;
}

.stat-card {
  border-radius: 8px;
  transition: all 0.3s ease;
  border: none;
  overflow: hidden;
  position: relative;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
}

.stat-pending::before {
  background-color: #e6a23c;
}

.stat-checked-in::before {
  background-color: #409eff;
}

.stat-completed::before {
  background-color: #67c23a;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.08);
}

.stat-card-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.stat-icon {
  font-size: 24px;
  margin-right: 10px;
}

.stat-pending .stat-icon {
  color: #e6a23c;
}

.stat-checked-in .stat-icon {
  color: #409eff;
}

.stat-completed .stat-icon {
  color: #67c23a;
}

.stat-title {
  font-size: 16px;
  color: #666;
  margin: 0;
  font-weight: 500;
}

.stat-value {
  font-size: 32px;
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
  transition: all 0.3s ease;
}

.stat-card:hover .stat-value {
  transform: scale(1.05);
}

.stat-desc {
  font-size: 12px;
  color: #999;
  padding-bottom: 5px;
}

.reservation-card {
  border-radius: 8px;
  background-color: #fff;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.card-title {
  font-size: 18px;
  color: #333;
  margin: 0;
  font-weight: 600;
}

.count-tag {
  margin-top: 5px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
  padding: 10px 0;
}

.operation-btn {
  margin-right: 5px;
  transition: all 0.2s ease;
}

.operation-btn:hover {
  transform: translateY(-2px);
}

.checkin-btn {
  background-color: #67c23a;
  border-color: #67c23a;
}

.checkin-btn:hover {
  background-color: #52c41a;
  border-color: #52c41a;
}

.checkout-btn {
  background-color: #409eff;
  border-color: #409eff;
}

.checkout-btn:hover {
  background-color: #1890ff;
  border-color: #1890ff;
}

.completed-btn {
  color: #67c23a;
}

.row-pending {
  background-color: #fff8e6;
  transition: background-color 0.3s ease;
}

.row-checked-in {
  background-color: #e8f4fd;
  transition: background-color 0.3s ease;
}

.row-completed {
  background-color: #f0f9eb;
  transition: background-color 0.3s ease;
}

.el-table__row:hover > td {
  background-color: rgba(0, 0, 0, 0.03) !important;
}

.success-dialog {
  border-radius: 10px;
  overflow: hidden;
}

.success-dialog .dialog-content {
  text-align: center;
  padding: 20px 0;
}

.success-icon {
  font-size: 48px;
  color: #67c23a;
  margin-bottom: 15px;
  animation: success-bounce 0.6s ease;
}

@keyframes success-bounce {
  0% { transform: scale(0.6); opacity: 0; }
  70% { transform: scale(1.1); }
  100% { transform: scale(1); opacity: 1; }
}

.success-message {
  font-size: 16px;
  color: #333;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .checkin-page {
    padding: 15px 10px;
  }
  
  .el-row {
    display: flex;
    flex-direction: column;
  }
  
  .el-col {
    width: 100% !important;
    margin-bottom: 15px;
  }
  
  .filter-form {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }
  
  .filter-select {
    width: 100%;
    margin-right: 0;
  }
  
  .el-form-item {
    width: 100%;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .stat-value {
    font-size: 28px;
  }
}
</style>