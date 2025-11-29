<template>
  <el-card style="margin: 15px; min-height: calc(100vh - 80px)">
    <div class="page-title">教室设备报修</div>
    
    <!-- 表单区域 -->
    <el-form 
      :model="form" 
      :rules="rules" 
      ref="formRef" 
      label-width="120px"
      class="search-form"
    >
      <el-form-item label="教学楼" prop="buildingId">
        <el-select 
          v-model="form.buildingId" 
          placeholder="请选择教学楼" 
          @change="handleBuildingChange"
          style="width: 100%"
        >
          <el-option 
            v-for="building in buildingList" 
            :key="building.buildingId" 
            :label="building.buildingName" 
            :value="building.buildingId"
          ></el-option>
        </el-select>
      </el-form-item>
      
      <el-form-item label="教室号" prop="classroomId">
        <el-select 
          v-model="form.classroomId" 
          placeholder="请选择教室号"
          style="width: 100%"
        >
          <el-option 
            v-for="classroom in classroomList" 
            :key="classroom.classroomId" 
            :label="classroom.classroomName || classroom.classroomId" 
            :value="classroom.classroomId"
          ></el-option>
        </el-select>
      </el-form-item>
      
      <el-form-item label="设备类型" prop="deviceType">
        <el-select 
          v-model="form.deviceType" 
          placeholder="请选择设备类型"
          style="width: 100%"
        >
          <el-option label="投影仪" value="投影仪"></el-option>
          <el-option label="音响设备" value="音响设备"></el-option>
          <el-option label="空调" value="空调"></el-option>
          <el-option label="桌椅" value="桌椅"></el-option>
          <el-option label="黑板/白板" value="黑板/白板"></el-option>
          <el-option label="照明设备" value="照明设备"></el-option>
          <el-option label="其他" value="其他"></el-option>
        </el-select>
      </el-form-item>
      
      <el-form-item label="问题描述" prop="problemDetail">
        <el-input 
          v-model="form.problemDetail" 
          type="textarea" 
          rows="5"
          placeholder="请详细描述设备故障情况（至少10个字）..."
        ></el-input>
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" @click="submitForm">提交报修</el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import request from "@/utils/request"; 
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';

const router = useRouter();

// 表单数据
const form = reactive({
  buildingId: '',
  classroomId: '',
  deviceType: '',
  problemDetail: ''
});

// 表单验证规则（不变）
const rules = {
  buildingId: [
    { required: true, message: '请选择教学楼', trigger: 'change' }
  ],
  classroomId: [
    { required: true, message: '请选择教室号', trigger: 'change' }
  ],
  deviceType: [
    { required: true, message: '请选择设备类型', trigger: 'change' }
  ],
  problemDetail: [
    { required: true, message: '请描述问题详情', trigger: 'blur' },
    { min: 10, message: '描述至少10个字符', trigger: 'blur' }
  ]
};

const formRef = ref(null);
const buildingList = ref([]);
const classroomList = ref([]);
const loading = ref(false);

// 获取教学楼列表（不变）
const getBuildingList = async () => {
  loading.value = true;
  try {
    const response = await request.get('/building/listAll'); 
    
    if (response.code === "0") {
      const allBuildings = response.data || [];
      const identity = sessionStorage.getItem('identity') || ''; 
      if (identity === 'stu') {
        buildingList.value = allBuildings.filter(b => ['J1', 'J2'].includes(b.buildingId));
      } else if (identity === 'teacher') {
        buildingList.value = allBuildings.filter(b => ['J3', 'J4'].includes(buildingId));
      } else {
        buildingList.value = allBuildings;
      }
      
      if (buildingList.value.length > 0) {
        form.buildingId = buildingList.value[0].buildingId;
        handleBuildingChange(form.buildingId);
      }
    } else {
      ElMessage.error('获取教学楼失败：' + response.msg);
    }
  } catch (error) {
    ElMessage.error('获取教学楼列表失败：' + error.message);
  } finally {
    loading.value = false;
  }
};

// 切换教学楼获取教室列表（不变）
const handleBuildingChange = async (buildingId) => {
  if (!buildingId) {
    classroomList.value = [];
    return;
  }
  
  const today = new Date();
  const dateStr = today.toISOString().split('T')[0];
  
  loading.value = true;
  try {
    const response = await request.get('/classroom/listByBuilding', {
      params: { buildingId, date: dateStr }
    });
    
    if (response.code === "0") {
      const allClassrooms = response.data || [];
      classroomList.value = allClassrooms.filter(classroom => 
        classroom.status === 1 || classroom.status === "1"
      );
      
      if (classroomList.value.length === 0) {
        ElMessage.info('该教学楼当天暂无可用教室');
      }
    } else {
      ElMessage.error(response.msg || '获取教室列表失败');
    }
  } catch (error) {
    ElMessage.error('获取教室列表失败：' + error.message);
  } finally {
    loading.value = false;
  }
};

// 提交报修（核心修改：适配路由守卫+完整子路由路径）
const submitForm = async () => {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
    
    // 1. 适配路由守卫的"user"键名（小写）
    let userJson = sessionStorage.getItem('User') || sessionStorage.getItem('user');
    // 若存在大写"User"，同步到小写"user"，让路由守卫识别登录状态
    if (sessionStorage.getItem('User') && !sessionStorage.getItem('user')) {
      sessionStorage.setItem('user', sessionStorage.getItem('User'));
      userJson = sessionStorage.getItem('user');
    }
    
    // 2. 适配identity解析（确保是字符串，不被JSON.parse误解析）
    let identity = sessionStorage.getItem('identity') || '';
    // 若identity被意外JSON.stringify，这里修正为原始字符串
    if (identity.startsWith('"') && identity.endsWith('"')) {
      identity = identity.slice(1, -1);
      sessionStorage.setItem('identity', identity);
    }
    
    // 3. 登录状态校验
    if (!userJson || !identity) {
      ElMessage.warning('请先登录');
      router.push('/Login');
      return;
    }
    
    const user = JSON.parse(userJson);
    const submitUsername = user.username;
    if (!submitUsername) {
      ElMessage.warning('用户信息异常，请重新登录');
      router.push('/Login');
      return;
    }
    
    const submitData = {
      ...form,
      username: submitUsername
    };
    
    const response = await request.post('/repair/classroom/submit', submitData);
    if (response.code === "0") {
      ElMessage.success('报修提交成功');
      resetForm();
      // 4. 跳转完整子路由路径（Layout的子路由需带父路径）
      router.push('/Layout/myClassroomRepairs');
    } else {
      ElMessage.error(response.msg || '提交失败');
    }
  } catch (error) {
    if (error.name === 'ValidationError') return;
    ElMessage.error('网络错误，提交失败');
  }
};

// 重置表单（不变）
const resetForm = () => {
  formRef.value?.resetFields();
  classroomList.value = [];
};

// 页面加载时初始化（不变）
onMounted(() => {
  getBuildingList();
});
</script>

<style scoped>
.page-title {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #333;
}

.search-form {
  margin-bottom: 10px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.el-form-item {
  margin-bottom: 20px;
}
</style>