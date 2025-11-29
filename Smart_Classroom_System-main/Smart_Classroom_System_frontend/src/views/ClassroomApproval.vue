<template>
  <div class="approval-container">
    <el-card>
      <div slot="header" class="card-header">
        <span>教室预约审批管理</span>
        <div class="filter-container">
          <!-- 教学楼筛选 -->
          <el-select 
            v-model="selectedBuilding" 
            placeholder="筛选教学楼" 
            clearable
            @change="handleFilterChange"
            style="width: 180px;"
          >
            <el-option label="J1教学楼" value="J1"></el-option>
            <el-option label="J2教学楼" value="J2"></el-option>
            <el-option label="J3教学楼" value="J3"></el-option>
            <el-option label="J4教学楼" value="J4"></el-option>
          </el-select>
          
          <!-- 预约日期筛选 -->
          <el-date-picker
            v-model="selectedDate"
            type="date"
            placeholder="选择预约日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            clearable
            @change="handleFilterChange"
            style="width: 180px;"
          ></el-date-picker>
          
          <!-- 身份筛选 -->
          <el-select 
            v-model="selectedIdentity" 
            placeholder="筛选身份" 
            clearable
            @change="handleFilterChange"
            style="width: 180px;"
          >
            <el-option label="学生" value="stu"></el-option>
            <el-option label="老师" value="teacher"></el-option>
          </el-select>
          
          <!-- 状态筛选 -->
          <el-select 
            v-model="selectedStatus" 
            placeholder="筛选状态" 
            clearable
            @change="handleFilterChange"
            style="width: 180px;"
          >
            <el-option label="待审核" value="0"></el-option>
            <el-option label="已通过" value="1"></el-option>
            <el-option label="已拒绝" value="2"></el-option>
            <el-option label="已过期" value="3"></el-option>
          </el-select>
          
          <!-- 搜索框 -->
          <el-input
            v-model="searchKeyword"
            placeholder="搜索申请人姓名/学号"
            clearable
            @clear="handleFilterChange"
            @keyup.enter="handleSearch"
            style="width: 200px;"
          >
            <template #append>
              <el-button icon="Search" size="small" @click="handleSearch"></el-button>
            </template>
          </el-input>
          
          <el-button 
            type="primary" 
            icon="Refresh" 
            size="small"
            @click="handleRefresh"
          >
            刷新
          </el-button>
        </div>
      </div>
      
      <el-table 
        :data="approvalList" 
        border 
        style="width: 100%; margin-top: 20px"
        v-loading="loading"
      >
        <el-table-column 
          prop="id" 
          label="预约ID" 
          width="80" 
          align="center"
        ></el-table-column>
        <el-table-column 
          prop="buildingId" 
          label="教学楼栋" 
          align="center"
          width="100"
        ></el-table-column>
        <el-table-column 
          prop="classroomId" 
          label="教室号" 
          align="center"
        ></el-table-column>
        <el-table-column 
          prop="reserveDate" 
          label="预约日期" 
          align="center"
        ></el-table-column>
        <el-table-column 
          prop="startTime" 
          label="开始时间" 
          align="center"
        ></el-table-column>
        <el-table-column 
          prop="endTime" 
          label="结束时间" 
          align="center"
        ></el-table-column>
        <el-table-column 
          prop="username" 
          label="申请人" 
          align="center"
        ></el-table-column>
        <el-table-column 
          prop="reserverName" 
          label="申请人姓名" 
          align="center"
        ></el-table-column>
        <el-table-column 
          prop="identity" 
          label="身份" 
          align="center"
          width="80"
          :formatter="formatIdentity"
        ></el-table-column>
        <el-table-column 
          prop="status" 
          label="状态" 
          align="center"
          width="100"
        >
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column 
          prop="reserveReason" 
          label="预约用途" 
          align="center"
        ></el-table-column>
        <el-table-column 
          prop="createTime" 
          label="申请时间" 
          align="center"
        ></el-table-column>
        <el-table-column prop="auditor" label="审批人" align="center" width="120">
          <template #default="scope">
            {{ scope.row.auditor || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="auditTime" label="审批时间" align="center" width="180">
          <template #default="scope">
            {{ scope.row.auditTime ? formatDate(scope.row.auditTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="adminRemark" label="审批意见" align="center" min-width="200">
          <template #default="scope">
            {{ scope.row.adminRemark || '-' }}
          </template>
        </el-table-column>
        <el-table-column 
          label="操作" 
          align="center"
          width="200"
        >
          <template #default="scope">
            <el-button 
              type="success" 
              size="small" 
              @click="openApprovalDialog(scope.row, 1)"
              :disabled="scope.row.status !== 0"
            >
              通过
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="openApprovalDialog(scope.row, 2)"
              :disabled="scope.row.status !== 0"
              style="margin-left: 10px"
            >
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination" v-if="total > 0">
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
    </el-card>

    <!-- 审批意见弹窗 -->
    <el-dialog 
      v-model="isDialogShow" 
      :title="currentStatus === 1 ? '通过审批' : '拒绝审批'" 
      width="400px"
      :close-on-click-modal="false"
    >
      <el-input
        v-model="approvalRemark"
        type="textarea"
        :rows="4"
        placeholder="请输入审批意见（必填）"
        style="width: 100%"
      ></el-input>
      <template #footer>
        <el-button @click="isDialogShow = false">取消</el-button>
        <el-button type="primary" @click="confirmApproval">确认审批</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import request from "@/utils/request";
import { ElMessage } from "element-plus";

export default {
  name: "ClassroomApproval",
  data() {
    return {
      approvalList: [],
      loading: false,
      pageNum: 1,
      pageSize: 10,
      total: 0,
      selectedIdentity: '',
      selectedStatus: '',
      selectedBuilding: '', // 教学楼筛选
      selectedDate: '', // 预约日期筛选
      searchKeyword: '', // 搜索关键词
      isDialogShow: false,
      currentRow: null,
      currentStatus: null, // 1=通过，2=拒绝
      approvalRemark: ''
    };
  },
  created() {
    this.loadApprovalList();
  },
  methods: {
    formatIdentity(row) {
      return row.identity === 'stu' ? '学生' : '老师';
    },
    formatDate(dateStr) {
      if (!dateStr) return '';
      const date = new Date(dateStr);
      return date.toLocaleString('zh-CN', { 
        year: 'numeric', 
        month: '2-digit', 
        day: '2-digit', 
        hour: '2-digit', 
        minute: '2-digit', 
        second: '2-digit' 
      });
    },
    getStatusText(status) {
      switch(status) {
        case 0: return '待审核';
        case 1: return '已通过';
        case 2: return '已拒绝';
        case 3: return '已过期';
        default: return '未知';
      }
    },
    getStatusTagType(status) {
      switch(status) {
        case 0: return 'warning';
        case 1: return 'success';
        case 2: return 'danger';
        case 3: return 'info';
        default: return 'info';
      }
    },
    async loadApprovalList() {
      this.loading = true;
      try {
        const params = {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          identity: this.selectedIdentity,
          status: this.selectedStatus,
          buildingId: this.selectedBuilding,
          reserveDate: this.selectedDate
        };
        
        // 处理搜索关键词，支持多字段搜索
        if (this.searchKeyword) {
          params.username = this.searchKeyword;
          params.reserverName = this.searchKeyword;
        }

        const response = await request.get("/classroom/reservation/pending", { params });
        
        if (response.code === "0") {
          this.approvalList = response.data.records || [];
          this.total = response.data.total || 0;
        } else {
          ElMessage.error("加载列表失败：" + response.msg);
        }
      } catch (error) {
        ElMessage.error("网络错误，请重试");
        console.error("列表加载失败：", error);
      } finally {
        this.loading = false;
      }
    },
    handleFilterChange() {
      this.pageNum = 1;
      this.loadApprovalList();
    },
    handleSearch() {
      this.pageNum = 1;
      this.loadApprovalList();
    },
    handleRefresh() {
      // 重置所有筛选条件
      this.selectedIdentity = '';
      this.selectedStatus = '';
      this.selectedBuilding = '';
      this.selectedDate = '';
      this.searchKeyword = '';
      this.pageNum = 1;
      this.loadApprovalList();
    },
    handleSizeChange(val) {
      this.pageSize = val;
      this.pageNum = 1;
      this.loadApprovalList();
    },
    handleCurrentChange(val) {
      this.pageNum = val;
      this.loadApprovalList();
    },
    openApprovalDialog(row, status) {
      this.currentRow = row;
      this.currentStatus = status;
      this.approvalRemark = '';
      this.isDialogShow = true;
    },
    async confirmApproval() {
      if (!this.approvalRemark.trim()) {
        ElMessage.warning("请输入审批意见！");
        return;
      }
      try {
        const response = await request.post("/classroom/reservation/audit", {
          id: this.currentRow.id,
          status: this.currentStatus,
          adminRemark: this.approvalRemark.trim()
        });
        if (response.code === "0") {
          ElMessage.success(this.currentStatus === 1 ? "审批通过成功" : "拒绝操作成功");
          this.isDialogShow = false;
          this.loadApprovalList();
        } else {
          ElMessage.error("操作失败：" + (response.msg || "未知错误"));
        }
      } catch (error) {
        ElMessage.error("网络请求失败，请重试");
        console.error("审批接口调用失败：", error);
      }
    }
  }
};
</script>

<style scoped>
.approval-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap; /* 适配小屏幕 */
  gap: 10px;
}

.filter-container {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap; /* 适配小屏幕 */
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>