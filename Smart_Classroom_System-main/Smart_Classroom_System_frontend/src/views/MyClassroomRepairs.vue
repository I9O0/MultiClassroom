<template>
  <div class="my-repairs-container">
    <el-card>
      <div slot="header">
        <h2>我的报修记录</h2>
      </div>
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="报修ID" width="80"></el-table-column>
        <el-table-column label="教学楼" width="100">
          <template #default="scope">
            {{ getBuildingName(scope.row.buildingId) }}
          </template>
        </el-table-column>
        <el-table-column prop="classroomId" label="教室号" width="100"></el-table-column>
        <el-table-column prop="deviceType" label="设备类型" width="120"></el-table-column>
        <el-table-column prop="problemDetail" label="问题描述"></el-table-column>
        
        <!-- 故障图片列 -->
        <el-table-column label="故障图片" width="120">
          <template #default="scope">
            <div v-if="scope.row.faultPhotos" class="image-count" @click="showImages(scope.row)">
              <i class="el-icon-picture"></i>
              <span>{{ getImageCount(scope.row.faultPhotos) }}张</span>
            </div>
            <span v-else>无</span>
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

    <!-- 详情对话框（含图片预览） -->
    <el-dialog title="报修详情" v-model="detailDialogVisible" width="600px">
      <el-descriptions column="1" border>
        <el-descriptions-item label="报修ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="教学楼">{{ getBuildingName(detailData.buildingId) }}</el-descriptions-item>
        <el-descriptions-item label="教室号">{{ detailData.classroomId }}</el-descriptions-item>
        <el-descriptions-item label="设备类型">{{ detailData.deviceType }}</el-descriptions-item>
        <el-descriptions-item label="问题描述">{{ detailData.problemDetail }}</el-descriptions-item>
        
        <!-- 故障图片展示 -->
        <el-descriptions-item label="故障图片" v-if="detailData.faultPhotos">
          <div class="image-preview">
            <el-image
              v-for="(img, index) in getImageList(detailData.faultPhotos)"
              :key="index"
              :src="getImageUrl(img)"
              :preview-src-list="getImageList(detailData.faultPhotos).map(url => getImageUrl(url))"
              class="preview-image"
              fit="cover"
              lazy
            ></el-image>
          </div>
        </el-descriptions-item>
        
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
    
    <!-- 图片查看弹窗 -->
    <el-dialog title="故障图片" v-model="imageDialogVisible" width="80%">
      <div class="dialog-image-container">
        <el-image
          v-for="(img, index) in currentImages"
          :key="index"
          :src="getImageUrl(img)"
          :preview-src-list="currentImages.map(url => getImageUrl(url))"
          class="dialog-image"
          fit="contain"
        ></el-image>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, reactive, toRefs } from 'vue';
import request from "@/utils/request";
import { ElMessage, ElLoading } from 'element-plus';
import { useRouter } from 'vue-router';

export default {
  setup() {
    const router = useRouter();

    // 状态映射
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
      buildingList: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      detailDialogVisible: false,
      detailData: {},
      showHandleResult: true,
      imageDialogVisible: false,
      currentImages: [],
      loading: false
    });

    const { 
      tableData, buildingList, pageNum, pageSize, total, 
      detailDialogVisible, detailData, showHandleResult,
      imageDialogVisible, currentImages, loading
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

    // 获取教学楼名称
    const getBuildingName = (buildingId) => {
      if (!buildingId) return '';
      const targetBuilding = buildingList.value.find(b => b.buildingId === buildingId);
      return targetBuilding ? targetBuilding.buildingName : buildingId;
    };

    // 获取图片数量
    const getImageCount = (photos) => {
      if (!photos) return 0;
      return photos.split(',').filter(img => img.trim()).length;
    };

    // 分割图片路径为数组
    const getImageList = (photos) => {
      if (!photos) return [];
      return photos.split(',').filter(img => img.trim());
    };

    // 拼接图片访问路径（完全移除不兼容语法，直接写死后端地址）
    const getImageUrl = (imagePath) => {
      if (!imagePath) return '';
      // ！！！替换成你的后端实际IP+端口（比如http://192.168.1.100:9090）
      const baseUrl = 'http://localhost:9090';
      if (imagePath.startsWith('http')) return imagePath;
      return baseUrl + '/file/access?path=' + encodeURIComponent(imagePath);
    };

    // 打开图片查看弹窗
    const showImages = (row) => {
      if (!row.faultPhotos) {
        ElMessage.info('该报修记录暂无故障图片');
        return;
      }
      currentImages.value = getImageList(row.faultPhotos);
      imageDialogVisible.value = true;
    };

    // 加载教学楼列表
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
      state.loading = true;
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
      } finally {
        state.loading = false;
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

    // 页面加载时初始化
    onMounted(() => {
      getBuildingList(); 
      getMyRepairs();
    });

    return {
      tableData,
      buildingList,
      pageNum,
      pageSize,
      total,
      detailDialogVisible,
      detailData,
      showHandleResult,
      imageDialogVisible,
      currentImages,
      loading,
      statusTextMap,
      statusTypeMap,
      formatDateTime,
      getBuildingName,
      getImageCount,
      getImageList,
      getImageUrl,
      showImages,
      getMyRepairs,
      showDetail,
      handleSizeChange,
      handleCurrentChange
    };
  }
};
</script>

<!-- 只替换原<style scoped>部分，其他代码保持不变 -->
<style scoped>
.my-repairs-container {
  padding: 20px;
}

.handle-result {
  color: #409eff;
  cursor: pointer;
  text-decoration: underline;
}

/* 图片相关样式（重点调整） */
.image-count {
  color: #409eff;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 4px 0;
  transition: color 0.2s;
}

.image-count:hover {
  color: #66b1ff;
}

/* 缩略图容器：限制每行最多3张，统一间距 */
.image-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 10px;
  max-height: 220px;
  overflow-y: auto;
  padding: 8px;
  box-sizing: border-box;
}

/* 缩略图：固定宽高+居中裁剪，避免变形 */
.preview-image {
  width: 120px;
  height: 120px;
  border-radius: 6px;
  cursor: zoom-in;
  object-fit: cover; /* 居中裁剪，保持比例 */
  box-sizing: border-box;
  border: 1px solid #eee;
}

/* 弹窗图片容器：垂直排列+居中 */
/* 弹窗图片容器：居中+自动换行 */
.dialog-image-container {
  max-height: 90vh;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-wrap: wrap; /* 多张图自动换行 */
  justify-content: center; /* 水平居中 */
  gap: 20px; /* 图片之间的间距 */
}

/* 弹窗图片：统一尺寸+保持比例 */
.dialog-image {
  width: 280px; /* 固定宽度，适配多张图排版 */
  height: 280px; /* 固定高度，统一尺寸 */
  object-fit: cover; /* 居中裁剪，避免变形 */
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

/* 滚动条美化 */
.image-preview::-webkit-scrollbar,
.dialog-image-container::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.image-preview::-webkit-scrollbar-thumb,
.dialog-image-container::-webkit-scrollbar-thumb {
  background-color: #ccc;
  border-radius: 3px;
}
</style>