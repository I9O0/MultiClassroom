<template>
  <div class="approval-container">
    <el-card>
      <div slot="header" class="card-header">
        <span>教室预约审批管理</span>
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
  prop="reserveReason" 
  label="预约用途" 
  align="center"
></el-table-column>
        <el-table-column 
          label="操作" 
          align="center"
          width="200"
        >
          <template #default="scope">
            <el-button 
              type="success" 
              size="small" 
              @click="handleApproval(scope.row, 1)"
              :disabled="scope.row.status !== 0"
            >
              通过
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleApproval(scope.row, 0)"
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
      total: 0
    };
  },
  created() {
    this.loadApprovalList();
  },
  methods: {
    // 加载待审批列表
    async loadApprovalList() {
      this.loading = true;
      try {
        const response = await request.get("/classroom/reservation/pending", {
          params: {
            pageNum: this.pageNum,
            pageSize: this.pageSize
          }
        });
        
        if (response.code === "0") {
          this.approvalList = response.data.records;
          this.total = response.data.total;
        } else {
          ElMessage.error("加载审批列表失败：" + response.msg);
        }
      } catch (error) {
        ElMessage.error("获取数据失败，请重试");
        console.error(error);
      } finally {
        this.loading = false;
      }
    },
    
    // 处理审批
    async handleApproval(row, status) {
      try {
        const response = await request.post("/classroom/reservation/audit", {
          id: row.id,
          status: status
        });
        
        if (response.code === "0") {
          ElMessage.success(status === 1 ? "审批通过" : "已拒绝");
          this.loadApprovalList(); // 重新加载列表
        } else {
          ElMessage.error(response.msg || "操作失败");
        }
      } catch (error) {
        ElMessage.error("操作失败，请重试");
        console.error(error);
      }
    },
    
    // 分页大小变化
    handleSizeChange(val) {
      this.pageSize = val;
      this.pageNum = 1;
      this.loadApprovalList();
    },
    
    // 当前页变化
    handleCurrentChange(val) {
      this.pageNum = val;
      this.loadApprovalList();
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
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>