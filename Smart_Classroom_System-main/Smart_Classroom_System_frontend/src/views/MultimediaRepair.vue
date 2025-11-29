<template>
  <div class="repair-container">
    <!-- 页面标题 -->
    <div class="page-title">多媒体教室设备报修系统</div>

    <!-- 报修表单卡片 -->
    <el-card class="repair-form-card" shadow="hover">
      <el-form 
        ref="repairFormRef" 
        :model="repairForm" 
        :rules="formRules" 
        label-width="130px"
        class="form-container"
      >
        <!-- 1. 教学楼选择 -->
        <el-form-item label="所属教学楼" prop="buildingId">
          <el-select 
            v-model="repairForm.buildingId" 
            placeholder="请选择教学楼" 
            clearable
            @change="handleBuildingChange"
            style="width: 100%;"
          >
            <el-option
              v-for="building in buildingList"
              :key="building.buildingId"
              :label="building.buildingName"
              :value="building.buildingId"
            />
          </el-select>
        </el-form-item>

        <!-- 2. 教室选择（依赖教学楼） -->
        <el-form-item label="具体教室" prop="classroomId">
          <el-select 
            v-model="repairForm.classroomId" 
            placeholder="请先选择教学楼" 
            clearable
            style="width: 100%;"
            :disabled="!repairForm.buildingId"
          >
            <el-option
              v-for="classroom in classroomList"
              :key="classroom.id"
              :label="classroom.classroomName || `${repairForm.buildingId}-${classroom.classroomId}`"
              :value="classroom.classroomId"
            />
          </el-select>
        </el-form-item>

        <!-- 3. 设备类型选择 -->
        <el-form-item label="故障设备类型" prop="deviceType">
          <el-select 
            v-model="repairForm.deviceType" 
            placeholder="请选择故障设备" 
            clearable
            style="width: 100%;"
          >
            <el-option label="投影仪" value="projector" />
            <el-option label="音响系统" value="speaker" />
            <el-option label="教师电脑" value="computer" />
            <el-option label="投影幕布" value="screen" />
            <el-option label="智能黑板" value="smartBoard" />
            <el-option label="其他设备" value="other" />
          </el-select>
        </el-form-item>

        <!-- 4. 故障描述 -->
        <el-form-item label="故障详细描述" prop="faultDesc">
          <el-input
            type="textarea"
            v-model="repairForm.faultDesc"
            placeholder="请描述故障现象（例如：投影仪开机后无画面，指示灯闪烁）"
            rows="4"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <!-- 5. 故障照片上传（可选） -->
        <el-form-item label="故障照片（可选）">
          <el-upload
            action="/api/upload"
            list-type="picture-card"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :limit="3"
            :file-list="uploadFileList"
            :on-exceed="handleExceed"
          >
            <el-icon><Plus /></el-icon>
            <div class="el-upload__text">点击上传</div>
          </el-upload>
          <div class="upload-tip">最多上传3张照片，支持jpg、png格式</div>
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item class="form-actions">
          <el-button type="primary" @click="submitRepair">提交报修申请</el-button>
          <el-button @click="resetForm">重置表单</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 我的报修记录 -->
    <el-card class="repair-records-card" shadow="hover" style="margin-top: 20px;">
      <div slot="header" class="records-header">
        <span>我的报修记录</span>
        <el-button 
          type="text" 
          @click="refreshRecords"
          size="small"
        >
          <el-icon><Refresh /></el-icon> 刷新
        </el-button>
      </div>

      <el-table 
        :data="repairRecords" 
        border 
        stripe
        :loading="recordsLoading"
        style="width: 100%;"
      >
        <el-table-column prop="id" label="报修单号" width="100" align="center" />
        <el-table-column prop="buildingId" label="教学楼" width="100" align="center" />
        <el-table-column prop="classroomId" label="教室" width="100" align="center" />
        <el-table-column prop="deviceType" label="设备类型" width="120" align="center">
          <template #default="scope">
            <el-tag>{{ formatDeviceType(scope.row.deviceType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="处理状态" width="120" align="center">
          <template #default="scope">
            <el-tag 
              :type="statusTypeMap[scope.row.status].type"
            >
              {{ statusTypeMap[scope.row.status].label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="200" align="center" />
        <el-table-column prop="handleNote" label="处理备注" min-width="200" />
        <el-table-column label="操作" width="120" align="center">
          <template #default="scope">
            <el-button 
              type="text" 
              size="small"
              @click="viewFaultPhotos(scope.row.faultPhoto)"
              :disabled="!scope.row.faultPhoto"
            >
              查看照片
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 无记录提示 -->
      <div v-if="repairRecords.length === 0 && !recordsLoading" class="no-records">
        <el-empty description="暂无报修记录" />
      </div>
    </el-card>

    <!-- 照片查看弹窗 -->
    <el-dialog 
      title="故障照片" 
      v-model="photoDialogVisible" 
      :width="photoDialogWidth"
    >
      <div class="photo-viewer">
        <img 
          v-for="(url, index) in currentPhotos" 
          :key="index" 
          :src="url" 
          alt="故障照片"
          class="fault-photo"
        >
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Picture } from '@element-plus/icons-vue'
import request from '@/utils/request'

// 表单数据
const repairForm = reactive({
  username: '', // 学生学号（从登录信息获取）
  buildingId: '', // 教学楼ID
  classroomId: '', // 教室号
  deviceType: '', // 设备类型
  faultDesc: '', // 故障描述
  faultPhoto: '' // 照片路径（多图用逗号分隔）
})

// 表单验证规则
const formRules = reactive({
  buildingId: [
    { required: true, message: '请选择教学楼', trigger: 'change' }
  ],
  classroomId: [
    { required: true, message: '请选择教室', trigger: 'change' }
  ],
  deviceType: [
    { required: true, message: '请选择故障设备类型', trigger: 'change' }
  ],
  faultDesc: [
    { required: true, message: '请描述故障现象', trigger: 'blur' },
    { min: 5, message: '描述至少5个字符', trigger: 'blur' }
  ]
})

// 页面状态数据
const buildingList = ref([]) // 教学楼列表
const classroomList = ref([]) // 教室列表
const uploadFileList = ref([]) // 上传的文件列表
const repairRecords = ref([]) // 报修记录列表
const recordsLoading = ref(false) // 记录加载状态
const repairFormRef = ref(null) // 表单引用

// 照片查看弹窗
const photoDialogVisible = ref(false)
const currentPhotos = ref([])
const photoDialogWidth = ref('80%')

// 状态类型映射（后端状态 -> 前端显示）
const statusTypeMap = {
  pending: { label: '待处理', type: 'warning' },
  repairing: { label: '维修中', type: 'info' },
  solved: { label: '已解决', type: 'success' }
}

// 初始化页面
onMounted(() => {
  // 获取当前登录学生信息（从sessionStorage）
  const userInfo = JSON.parse(sessionStorage.getItem('user'))
  if (userInfo && userInfo.username) {
    repairForm.username = userInfo.username
    loadBuildings() // 加载教学楼列表
    loadRepairRecords() // 加载我的报修记录
  } else {
    // 未登录，跳转到登录页
    ElMessageBox.alert('请先登录系统', '未登录', {
      confirmButtonText: '前往登录',
      callback: () => {
        window.location.href = '/#/login' // 替换为实际登录页路由
      }
    })
  }
})

// 加载所有教学楼
const loadBuildings = async () => {
  try {
    const res = await request.get('/building/listAll')
    buildingList.value = res.data || []
  } catch (err) {
    console.error('加载教学楼失败：', err)
  }
}

// 切换教学楼时，加载对应教室
const handleBuildingChange = async (buildingId) => {
  if (!buildingId) {
    classroomList.value = []
    return
  }
  try {
    const res = await request.get('/classroom/listByBuilding', {
      params: { buildingId }
    })
    classroomList.value = res.data || []
  } catch (err) {
    console.error('加载教室失败：', err)
    classroomList.value = []
  }
}

// 提交报修申请
const submitRepair = async () => {
  // 表单验证
  repairFormRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      // 调用后端提交接口
      await request.post('/multimedia/repair/submit', repairForm)
      ElMessage.success('报修申请提交成功！维修人员将尽快处理')
      resetForm() // 重置表单
      loadRepairRecords() // 刷新报修记录
    } catch (err) {
      console.error('提交报修失败：', err)
    }
  })
}

// 重置表单
const resetForm = () => {
  repairFormRef.value.resetFields()
  uploadFileList.value = []
  repairForm.faultPhoto = ''
}

// 加载我的报修记录
const loadRepairRecords = async () => {
  if (!repairForm.username) return

  recordsLoading.value = true
  try {
    const res = await request.get('/multimedia/repair/myRecords', {
      params: { username: repairForm.username }
    })
    repairRecords.value = res.data || []
  } catch (err) {
    console.error('加载报修记录失败：', err)
    repairRecords.value = []
  } finally {
    recordsLoading.value = false
  }
}

// 刷新报修记录
const refreshRecords = () => {
  loadRepairRecords()
  ElMessage.success('已刷新记录')
}

// 处理照片上传成功
const handleUploadSuccess = (response) => {
  if (response.code === '0') {
    // 拼接照片路径（多图用逗号分隔）
    repairForm.faultPhoto = repairForm.faultPhoto
      ? `${repairForm.faultPhoto},${response.data}`
      : response.data
    ElMessage.success('照片上传成功')
  }
}

// 处理上传失败
const handleUploadError = () => {
  ElMessage.error('照片上传失败，请重试')
}

// 处理文件超出限制
const handleExceed = () => {
  ElMessage.warning('最多只能上传3张照片')
}

// 格式化设备类型显示
const formatDeviceType = (type) => {
  const typeMap = {
    projector: '投影仪',
    speaker: '音响系统',
    computer: '教师电脑',
    screen: '投影幕布',
    smartBoard: '智能黑板',
    other: '其他设备'
  }
  return typeMap[type] || type
}

// 查看故障照片
const viewFaultPhotos = (photoStr) => {
  if (!photoStr) return
  // 分割多图路径（后端存储格式：url1,url2,url3）
  currentPhotos.value = photoStr.split(',').map(url => {
    // 拼接完整图片路径（如果后端返回的是相对路径）
    return url.startsWith('http') ? url : `/api${url}`
  })
  photoDialogVisible.value = true
}
</script>

<style scoped>
.repair-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  font-size: 22px;
  font-weight: bold;
  color: #333;
  margin-bottom: 20px;
  text-align: center;
}

.repair-form-card {
  margin-bottom: 30px;
}

.form-container {
  padding: 10px 20px;
}

.form-actions {
  margin-top: 20px;
  text-align: center;
}

.records-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.upload-tip {
  margin-top: 10px;
  color: #666;
  font-size: 12px;
}

.no-records {
  padding: 50px 0;
  text-align: center;
}

.photo-viewer {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  max-height: 600px;
  overflow-y: auto;
  justify-content: center;
}

.fault-photo {
  max-width: 300px;
  max-height: 300px;
  object-fit: contain;
  border: 1px solid #eee;
  border-radius: 4px;
}
</style>