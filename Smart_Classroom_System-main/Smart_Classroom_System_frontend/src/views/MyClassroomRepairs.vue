<template>
  <div class="my-repairs-container">
    <el-card>
      <div slot="header">
        <h2>我的报修记录</h2>
      </div>
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="id" label="报修ID" width="80"></el-table-column>
        <!-- 修正：显示教学楼名称（替换原buildingId显示） -->
        <el-table-column label="教学楼" width="100">
          <template #default="scope">
            {{ getBuildingName(scope.row.buildingId) }}
          </template>
        </el-table-column>
        <el-table-column prop="classroomId" label="教室号" width="100"></el-table-column>
        <el-table-column prop="deviceType" label="设备类型" width="120"></el-table-column>
        <el-table-column prop="problemDetail" label="问题描述"></el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="statusTypeMap[scope.row.status]">
              {{ statusTextMap[scope.row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="处理结果" v-if="showHandleResult">
          <template #default="scope">
            <el-tooltip v-if="scope.row.handleResult" content="点击查看详情" placement="top">
              <div class="handle-result" @click="showDetail(scope.row)">
                {{ scope.row.handleResult.length > 20 ? scope.row.handleResult.slice(0, 20) + '...' : scope.row.handleResult }}
              </div>
            </el-tooltip>
            <span v-else>暂无</span>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pageNum"
        :page-sizes="[5, 10, 20]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total">
      </el-pagination>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog title="报修详情" :visible.sync="detailDialogVisible" width="600px">
      <el-descriptions column="1" border>
        <el-descriptions-item label="报修ID">{{ detailData.id }}</el-descriptions-item>
        <!-- 修正：详情显示教学楼名称 -->
        <el-descriptions-item label="教学楼">{{ getBuildingName(detailData.buildingId) }}</el-descriptions-item>
        <el-descriptions-item label="教室号">{{ detailData.classroomId }}</el-descriptions-item>
        <el-descriptions-item label="设备类型">{{ detailData.deviceType }}</el-descriptions-item>
        <el-descriptions-item label="问题描述">{{ detailData.problemDetail }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ formatDateTime(detailData.submitTime) }}</el-descriptions-item>
        <el-descriptions-item label="处理状态">
          <el-tag :type="statusTypeMap[detailData.status]">
            {{ statusTextMap[detailData.status] }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="处理人" v-if="detailData.handlerName">{{ detailData.handlerName }}</el-descriptions-item>
        <el-descriptions-item label="处理时间" v-if="detailData.handleTime">{{ formatDateTime(detailData.handleTime) }}</el-descriptions-item>
        <el-descriptions-item label="处理结果" v-if="detailData.handleResult">{{ detailData.handleResult }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, toRefs } from 'vue';
import request from "@/utils/request";
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';

const router = useRouter();

// 状态映射（整数匹配后端）
const statusTextMap = {
  0: '待处理',
  1: '维修中',
  2: '已解决'
};

const statusTypeMap = {
  0: 'warning',
  1: 'info',
  2: 'success'
};

// 响应式数据
const state = reactive({
  tableData: [],
  buildingList: [], // 新增：缓存教学楼列表
  pageNum: 1,
  pageSize: 10,
  total: 0,
  detailDialogVisible: false,
  detailData: {},
  showHandleResult: true
});

const { 
  tableData, buildingList, pageNum, pageSize, total, 
  detailDialogVisible, detailData, showHandleResult
} = toRefs(state);

// 格式化时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '';
  const normalized = dateTime.replace('T', ' ');
  return new Date(normalized).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
};

// 核心新增：本地匹配教学楼名称（替代/getBuildingName接口）
const getBuildingName = (buildingId) => {
  if (!buildingId) return '';
  const targetBuilding = buildingList.value.find(b => b.buildingId === buildingId);
  return targetBuilding ? targetBuilding.buildingName : buildingId;
};

// 新增：加载教学楼列表（用于名称匹配）
const getBuildingList = async () => {
  try {
    const response = await request.get('/building/listAll');
    if (response.code === "0") {
      buildingList.value = response.data || [];
    }
  } catch (error) {
    console.error('获取教学楼列表失败', error);
  }
};

// 获取个人报修记录
const getMyRepairs = async () => {
  try {
    const userJson = sessionStorage.getItem('User') || sessionStorage.getItem('user');
    if (!userJson) {
      ElMessage.warning('请先登录');
      router.push('/Login');
      return;
    }
    const user = JSON.parse(userJson);
    const username = user.username;
    if (!username) {
      ElMessage.warning('用户信息异常，请重新登录');
      router.push('/Login');
      return;
    }
    
    const response = await request.get('/repair/classroom/myRecords', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        reporterId: username
      }
    });
    
    if (response.code === "0") {
      tableData.value = response.data.records || [];
      total.value = response.data.total || 0;
    } else {
      ElMessage.error(response.msg || '获取记录失败');
    }
  } catch (error) {
    if (error.response?.data?.msg === '请先登录') {
      ElMessage.warning('登录已失效，请重新登录');
      router.push('/Login');
    } else {
      ElMessage.error('网络错误，获取记录失败：' + (error.message || ''));
    }
  }
};

// 查看详情
const showDetail = (row) => {
  detailData.value = { ...row };
  detailDialogVisible.value = true;
};

// 分页方法
const handleSizeChange = (val) => {
  pageSize.value = val;
  getMyRepairs();
};

const handleCurrentChange = (val) => {
  pageNum.value = val;
  getMyRepairs();
};

// 页面加载时初始化（先加载教学楼列表）
onMounted(() => {
  getBuildingList(); // 优先加载，用于名称匹配
  getMyRepairs();
});
</script>

<style scoped>
.my-repairs-container {
  padding: 20px;
}

.handle-result {
  color: #409eff;
  cursor: pointer;
  text-decoration: underline;
}
</style>