<template>
  <div class="repair-manage-container">
    <el-card>
      <div slot="header" class="card-header">
        <h2>教室报修管理</h2>
        <div class="filter-container">
          <!-- 状态筛选 -->
          <el-select v-model="status" placeholder="全部状态" @change="loadRepairList">
            <el-option label="待处理" :value="0"></el-option>
            <el-option label="处理中" :value="1"></el-option>
            <el-option label="已解决" :value="2"></el-option>
          </el-select>
          <!-- 教学楼筛选（修复key警告） -->
          <el-select v-model="buildingId" placeholder="全部教学楼" @change="loadRepairList">
            <el-option label="全部教学楼" value=""></el-option>
            <el-option 
              v-for="building in visibleBuildings" 
              :key="`building-${building}`" 
              :label="building + '教学楼'" 
              :value="building"
            ></el-option>
          </el-select>
        </div>
      </div>

      <!-- 统计卡片 -->
      <el-row :gutter="16" style="margin-bottom: 20px">
        <el-col :span="6">
          <el-card :body-style="{ padding: '15px' }" class="stat-card">
            <h4>待处理</h4>
            <p>{{ statData.pending || 0 }}</p>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card :body-style="{ padding: '15px' }" class="stat-card">
            <h4>处理中</h4>
            <p>{{ statData.repairing || 0 }}</p>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card :body-style="{ padding: '15px' }" class="stat-card">
            <h4>已解决</h4>
            <p>{{ statData.solved || 0 }}</p>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card :body-style="{ padding: '15px' }" class="stat-card">
            <h4>总计</h4>
            <p>{{ (statData.pending || 0) + (statData.repairing || 0) + (statData.solved || 0) }}</p>
          </el-card>
        </el-col>
      </el-row>

      <!-- 报修列表 -->
      <el-table 
        :data="tableData.records" 
        border 
        style="width: 100%"
        v-loading="loading"
        element-loading-text="加载中..."
      >
        <template #empty>
          <div class="empty-state">
            <el-icon :size="48"><DocumentRemove /></el-icon>
            <p>暂无报修记录</p>
          </div>
        </template>
        
        <el-table-column prop="id" label="报修ID" width="80"></el-table-column>
        <el-table-column prop="buildingId" label="教学楼" width="100"></el-table-column>
        <el-table-column prop="classroomId" label="教室号" width="100"></el-table-column>
        <el-table-column prop="deviceType" label="设备类型" width="120"></el-table-column>
        <el-table-column prop="problemDetail" label="问题描述"></el-table-column>
        <el-table-column prop="reporterName" label="报修人" width="100"></el-table-column>
        <el-table-column prop="reporterRole" label="角色" width="80">
          <template #default="scope">
            {{ scope.row.reporterRole === 'stu' ? '学生' : '教师' }}
          </template>
        </el-table-column>
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
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <!-- 允许待处理(0)和处理中(1)状态显示处理按钮 -->
            <el-button 
              v-if="scope.row.status === 0 || scope.row.status === 1" 
              type="primary" 
              size="small" 
              @click="handleRepair(scope.row)"
              :loading="scope.row.isHandling"
            >
              处理
            </el-button>
            <el-button 
              v-else 
              type="info" 
              size="small" 
              @click="showDetail(scope.row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pageNum"
        :page-sizes="[5, 10, 20]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="tableData.total">
      </el-pagination>
    </el-card>

    <!-- 处理报修对话框（新增图片查看） -->
    <el-dialog title="处理报修" v-model="handleDialogVisible" width="600px">
      <el-form :model="handleForm" label-width="80px" ref="handleFormRef" :rules="handleRules">
        <el-form-item label="报修ID">
          <el-input v-model="handleForm.id" disabled></el-input>
        </el-form-item>
        <el-form-item label="教学楼">
          <el-input v-model="handleForm.buildingId" disabled></el-input>
        </el-form-item>
        <el-form-item label="教室号">
          <el-input v-model="handleForm.classroomId" disabled></el-input>
        </el-form-item>
        <el-form-item label="问题描述">
          <el-input v-model="handleForm.problemDetail" type="textarea" rows="3" disabled></el-input>
        </el-form-item>
        
        <!-- 新增：故障图片显示 -->
        <el-form-item label="故障图片" v-if="handleForm.faultPhotos && getImageList(handleForm.faultPhotos).length">
          <div class="image-preview">
            <el-image
              v-for="(img, index) in getImageList(handleForm.faultPhotos)"
              :key="index"
              :src="getImageUrl(img)"
              :preview-src-list="getImageList(handleForm.faultPhotos).map(url => getImageUrl(url))"
              fit="cover"
              class="preview-image"
              lazy
            ></el-image>
          </div>
        </el-form-item>
        
        <!-- 修复：状态值改为数字1/2，匹配后端 -->
        <el-form-item label="处理状态" prop="status">
          <el-radio-group v-model="handleForm.status">
            <el-radio :label="1">处理中</el-radio>
            <el-radio :label="2">已解决</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理结果" prop="handleResult">
          <el-input 
            v-model="handleForm.handleResult" 
            type="textarea" 
            rows="4" 
            maxlength="500" 
            show-word-limit
            placeholder="请描述处理过程和结果..."
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="handleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle" :loading="isSubmitting">提交处理</el-button>
      </div>
    </el-dialog>

    <!-- 详情对话框（新增图片查看） -->
    <el-dialog title="报修详情" v-model="detailDialogVisible" width="600px">
      <el-descriptions column="1" border>
        <el-descriptions-item label="报修ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="教学楼">{{ detailData.buildingId }}</el-descriptions-item>
        <el-descriptions-item label="教室号">{{ detailData.classroomId }}</el-descriptions-item>
        <el-descriptions-item label="设备类型">{{ detailData.deviceType }}</el-descriptions-item>
        <el-descriptions-item label="问题描述">{{ detailData.problemDetail }}</el-descriptions-item>
        
        <!-- 新增：故障图片显示 -->
        <el-descriptions-item label="故障图片" v-if="detailData.faultPhotos && getImageList(detailData.faultPhotos).length">
          <div class="image-preview">
            <el-image
              v-for="(img, index) in getImageList(detailData.faultPhotos)"
              :key="index"
              :src="getImageUrl(img)"
              :preview-src-list="getImageList(detailData.faultPhotos).map(url => getImageUrl(url))"
              fit="cover"
              class="preview-image"
              lazy
            ></el-image>
          </div>
        </el-descriptions-item>
        
        <el-descriptions-item label="报修人">{{ detailData.reporterName }}</el-descriptions-item>
        <el-descriptions-item label="报修角色">{{ detailData.reporterRole === 'stu' ? '学生' : '教师' }}</el-descriptions-item>
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
import { ref, onMounted, reactive, toRefs, watch } from 'vue';
import request from "@/utils/request";
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRouter } from 'vue-router';
import dayjs from 'dayjs';
import { DocumentRemove } from '@element-plus/icons-vue';

const router = useRouter();

// 状态映射（与后端一致：0=待处理，1=处理中，2=已解决）
const statusTextMap = { 0: '待处理', 1: '处理中', 2: '已解决' };
const statusTypeMap = { 0: 'warning', 1: 'info', 2: 'success' };

// 响应式数据
const state = reactive({
  tableData: { records: [], total: 0 },
  statData: { pending: 0, repairing: 0, solved: 0 },
  visibleBuildings: [],
  pageNum: 1,
  pageSize: 10,
  status: '',
  buildingId: '',
  loading: false,
  isSubmitting: false,
  handleDialogVisible: false,
  detailDialogVisible: false,
  handleForm: {
    id: '',
    buildingId: '',
    classroomId: '',
    problemDetail: '',
    faultPhotos: '',
    status: 1, // 修复：默认值改为数字1（处理中）
    handleResult: ''
  },
  detailData: {},
  handleRules: {
    status: [{ required: true, message: '请选择处理状态', trigger: 'change' }],
    handleResult: [
      { required: true, message: '请输入处理结果', trigger: 'blur' },
      { max: 500, message: '处理结果不能超过500字', trigger: 'blur' }
    ]
  }
});

const { 
  tableData, statData, visibleBuildings, pageNum, pageSize, status,
  buildingId, loading, isSubmitting, handleDialogVisible, detailDialogVisible,
  handleForm, detailData, handleRules
} = toRefs(state);

const handleFormRef = ref(null);

// 格式化时间（兼容空值）
const formatDateTime = (dateTime) => {
  if (!dateTime) return '';
  try {
    return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss');
  } catch (error) {
    return dateTime;
  }
};

// 新增：处理图片路径（核心！替换为你的后端文件访问接口地址）
const getImageUrl = (imagePath) => {
  if (!imagePath) return '';
  // 必改：替换为后端实际的文件访问接口（比如http://127.0.0.1:8080）
  const baseUrl = 'http://localhost:9090'; 
  if (imagePath.startsWith('http')) return imagePath;
  return `${baseUrl}/file/access?path=${encodeURIComponent(imagePath)}`;
};

// 新增：分割图片路径（去重+过滤空值）
const getImageList = (photos) => {
  if (!photos) return [];
  return [...new Set(photos.split(',').filter(img => img.trim()))];
};

// 加载可见教学楼
const loadVisibleBuildings = async () => {
  try {
    console.log('开始加载可见教学楼');
    const response = await request.get('/repair/classroom/getVisibleBuildings');
    if (response.code === "0") {
      visibleBuildings.value = response.data;
      console.log('可见教学楼加载完成:', response.data);
    } else {
      ElMessage.error('获取教学楼权限失败: ' + (response.msg || '未知错误'));
    }
  } catch (error) {
    // 404友好提示
    if (error.response?.status === 404) {
      ElMessage.error('接口不存在：/repair/classroom/getVisibleBuildings，请确认后端接口路径');
    }
    console.error('获取可见教学楼失败:', error);
    ElMessage.error('加载教学楼列表失败，请刷新页面重试');
  }
};

// 加载报修列表（修复参数处理，避免传undefined）
const loadRepairList = async () => {
  loading.value = true;
  try {
    console.log('开始加载报修列表，参数:', {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      status: status.value,
      buildingId: buildingId.value
    });
    
    // 构建查询参数（仅传有值的参数）
    const queryParams = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    };
    if (status.value !== '') queryParams.status = status.value;
    if (buildingId.value) queryParams.buildingId = buildingId.value;
    
    const response = await request.get('/repair/classroom/admin/all', {
      params: queryParams
    });

    if (response.code === "0") {
      tableData.value = response.data;
      console.log('报修列表加载完成，共', response.data.total, '条记录');
    } else {
      ElMessage.error(response.msg || '获取报修列表失败');
    }
  } catch (error) {
    // 404友好提示
    if (error.response?.status === 404) {
      ElMessage.error('接口不存在：/repair/classroom/admin/all，请确认后端接口路径');
    }
    console.error('加载报修列表失败:', error);
    ElMessage.error(error.response?.data?.msg || '网络错误，请重试');
  } finally {
    loading.value = false;
  }
};

// 加载统计数据
const loadStatData = async () => {
  try {
    console.log('开始加载统计数据');
    const response = await request.get('/repair/classroom/countByStatus');
    if (response.code === "0") {
      statData.value = response.data;
      console.log('统计数据加载完成:', response.data);
    } else {
      ElMessage.error('获取统计数据失败: ' + (response.msg || '未知错误'));
    }
  } catch (error) {
    if (error.response?.status === 404) {
      ElMessage.error('接口不存在：/repair/classroom/countByStatus，请确认后端接口路径');
    }
    console.error('统计数据加载失败:', error);
  }
};

// 处理报修（修复状态默认值为数字）
const handleRepair = (row) => {
  console.log('准备处理报修记录:', row);
  try {
    if (!row || !row.id) {
      ElMessage.error('获取报修信息失败，记录不存在');
      return;
    }
    
    // 根据当前状态设置默认处理状态（数字值）
    const defaultStatus = row.status === 0 ? 1 : 2;
    
    // 填充表单数据（新增faultPhotos）
    handleForm.value = {
      id: row.id || '',
      buildingId: row.buildingId || '',
      classroomId: row.classroomId || '',
      problemDetail: row.problemDetail || '',
      faultPhotos: row.faultPhotos || '', // 新增：传递图片路径
      status: defaultStatus,
      handleResult: ''
    };
    
    handleDialogVisible.value = true;
    console.log('处理对话框已显示');
  } catch (error) {
    console.error('处理报修时出错:', error);
    ElMessage.error('打开处理窗口失败，请重试');
  }
};

// 提交处理结果（修复状态值为数字，匹配后端）
const submitHandle = async () => {
  if (!handleFormRef.value) return;
  
  try {
    // 表单验证
    await handleFormRef.value.validate();
    
    // 确认操作
    await ElMessageBox.confirm(
      `确定要将状态改为${handleForm.value.status === 1 ? '处理中' : '已解决'}吗？`,
      '确认操作',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    );

    isSubmitting.value = true;
    console.log('提交处理结果:', handleForm.value);
    
    const response = await request.put('/repair/classroom/handle', {
      id: handleForm.value.id,
      status: handleForm.value.status, // 提交数字状态
      handleResult: handleForm.value.handleResult
    });

    if (response.code === "0") {
      ElMessage.success('处理成功');
      handleDialogVisible.value = false;
      loadRepairList();
      loadStatData();
    } else {
      ElMessage.error(response.msg || '处理失败');
    }
  } catch (error) {
    if (error.name === 'ValidationError') return;
    if (error === 'cancel') return;
    if (error.response?.status === 404) {
      ElMessage.error('接口不存在：/repair/classroom/handle，请确认后端接口路径');
    }
    console.error('提交处理结果失败:', error);
    ElMessage.error('处理失败，请重试');
  } finally {
    isSubmitting.value = false;
  }
};

// 查看详情（传递图片路径）
const showDetail = (row) => {
  console.log('查看报修详情:', row);
  detailData.value = { ...row };
  detailDialogVisible.value = true;
};

// 分页事件
const handleSizeChange = (val) => {
  pageSize.value = val;
  loadRepairList();
};
const handleCurrentChange = (val) => {
  pageNum.value = val;
  loadRepairList();
};

// 页面初始化（修复identity解析错误）
onMounted(async () => {
  console.log('页面开始初始化');
  
  // 修复：安全解析identity（避免JSON.parse报错）
  let identity = '';
  try {
    const identityStr = sessionStorage.getItem('identity');
    identity = identityStr ? JSON.parse(identityStr) : '';
  } catch (error) {
    identity = '';
    console.error('解析身份信息失败:', error);
  }
  console.log('当前用户身份:', identity);

  // 检查登录状态
  let user = null;
  try {
    const userStr = sessionStorage.getItem('user');
    user = userStr ? JSON.parse(userStr) : null;
  } catch (error) {
    user = null;
    console.error('解析用户信息失败:', error);
  }
  const isLoggedIn = user && user.username;
  console.log('登录状态:', isLoggedIn ? '已登录' : '未登录');

  // 未登录处理
  if (!isLoggedIn) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }

  // 权限校验
  if (!['admin', 'manager'].includes(identity)) {
    ElMessage.error('权限不足：仅管理员和教室管理员可访问');
    router.push('/home');
    return;
  }

  // 加载数据
  try {
    await Promise.all([
      loadVisibleBuildings(),
      loadRepairList(),
      loadStatData()
    ]);
    console.log('页面初始化完成');
  } catch (error) {
    console.error('初始化数据加载失败:', error);
    ElMessage.error('页面初始化失败，请刷新重试');
  }
});

// 监听对话框关闭，重置表单
watch(handleDialogVisible, (newVal) => {
  if (!newVal && handleFormRef.value) {
    handleFormRef.value.resetFields();
  }
});
</script>

<style scoped>
.repair-manage-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}
.filter-container {
  display: flex;
  gap: 10px;
}
.el-select { width: 180px; }
.stat-card {
  text-align: center;
  color: #333;
  transition: transform 0.2s;
}
.stat-card:hover {
  transform: translateY(-5px);
}
.stat-card h4 { margin: 0 0 10px; font-size: 14px; }
.stat-card p { margin: 0; font-size: 24px; color: #1890ff; }
.empty-state {
  text-align: center;
  padding: 30px 0;
  color: #666;
}
.empty-state p { margin-top: 10px; font-size: 14px; }
::v-deep .el-dialog__body { 
  max-height: 70vh; 
  overflow-y: auto; 
  padding: 20px;
}
::v-deep .el-button--primary.is-loading {
  pointer-events: none;
}

/* 新增：图片预览样式 */
.image-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-top: 10px;
  max-height: 240px;
  overflow-y: auto;
  padding: 8px 0;
}
.preview-image {
  width: 120px;
  height: 120px;
  border-radius: 6px;
  cursor: zoom-in;
  object-fit: cover; /* 固定尺寸，避免变形 */
  border: 1px solid #eee;
}
/* 图片预览滚动条美化 */
.image-preview::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
.image-preview::-webkit-scrollbar-thumb {
  background-color: #ddd;
  border-radius: 3px;
}
</style>