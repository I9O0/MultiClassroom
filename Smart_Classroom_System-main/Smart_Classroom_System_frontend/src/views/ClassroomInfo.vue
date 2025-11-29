<template>
  <div class="container">
    <!-- 权限校验：无权限提示 -->
    <div v-if="!hasPermission">
      <el-alert title="无权限访问教室信息" type="error" show-icon />
    </div>

    <div v-else>
      <!-- 操作按钮区（仅管理员可见） -->
      <div class="button-group" v-if="isAdmin">
        <el-button type="primary" @click="openAddDialog">新增教室</el-button>
        <el-button type="warning" @click="handleRefresh">刷新</el-button>
      </div>

      <!-- 筛选区（日期+教学楼） -->
      <el-form inline :model="filterForm" class="filter-form">
        <el-form-item label="查询日期">
          <el-date-picker
            v-model="filterForm.queryDate"
            type="date"
            placeholder="选择日期"
            :disabled-date="disabledDate"
            @change="handleDateChange"
            style="width: 180px; margin-right: 10px"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="教学楼">
          <el-select 
            v-model="filterForm.buildingId" 
            placeholder="选择教学楼"
            style="width: 150px; margin-right: 10px"
          >
            <el-option 
              v-for="building in buildingList" 
              :key="building.buildingId"
              :label="building.buildingName"
              :value="building.buildingId"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="success" @click="filterClassrooms">查询</el-button>
        </el-form-item>
      </el-form>

      <!-- 教室列表 -->
      <el-table 
        :data="filteredClassroomList" 
        border 
        stripe 
        style="width: 100%; margin-top: 15px"
        v-loading="loading"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="buildingId" label="教学楼" width="100" />
        <el-table-column prop="classroomId" label="教室号" width="100" />
        <el-table-column prop="classroomName" label="教室名称" />
        <el-table-column prop="seatCount" label="座位数" width="100" />
        <el-table-column prop="multimediaType" label="设备情况" 
          :formatter="(row) => row.multimediaType || '无'"
        />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <!-- 管理员可见状态开关，宿管仅显示文本 -->
            <template v-if="isAdmin">
              <el-switch
                v-model="scope.row.status"
                :active-value="1"
                :inactive-value="0"
                @change="handleStatusChange(scope.row)"
                active-text="可用"
                inactive-text="禁用"
              />
            </template>
            <template v-else>
              <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                {{ scope.row.status === 1 ? '可用' : '禁用' }}
              </el-tag>
            </template>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="scope">
            <!-- 通用：查看可用时段 -->
            <el-button 
              type="primary" 
              size="small" 
              @click="viewAvailableTime(scope.row)"
              :disabled="scope.row.status !== 1"
            >
              查看时段详情
            </el-button>
            
            <!-- 管理员特有：编辑和删除 -->
            <template v-if="isAdmin">
              <el-button 
                type="warning" 
                size="small" 
                @click="openEditDialog(scope.row)"
                style="margin-left: 5px"
              >
                编辑
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                @click="handleDelete(scope.row.id)"
                style="margin-left: 5px"
              >
                删除
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pageNum"
        :page-sizes="[10, 20, 50]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        style="margin-top: 15px"
      />
    </div>

    <!-- 时段预约详情弹窗 -->
    <el-dialog 
      title="时段预约详情" 
      v-model="timeDialogVisible" 
      width="600px"
    >
      <div class="dialog-content">
        <p><strong>教室：</strong>{{ currentClassroom?.buildingId }}-{{ currentClassroom?.classroomId }}（{{ currentClassroom?.classroomName }}）</p>
        <p><strong>查询日期：</strong>
          <el-date-picker
            v-model="selectedDate"
            type="date"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            :disabled-date="disabledDate"
            @change="fetchAvailableTime"
          ></el-date-picker>
        </p>
        <!-- 时段详情渲染 -->
        <div v-if="selectedDate" class="time-slots">
          <div v-for="slot in timeSlotOptions" :key="slot" class="time-slot-item">
            <el-tag 
              :type="getReservationBySlot(slot) ? 'danger' : 'success'"
              style="margin: 5px; padding: 8px 15px;"
            >
              {{ slot }}
            </el-tag>
            <div v-if="getReservationBySlot(slot)" class="reservation-info">
              （{{ getReservationBySlot(slot).status === 1 ? '已通过' : '待审核' }}：
              {{ getReservationBySlot(slot).studentName }} - 
              {{ getReservationBySlot(slot).reserveReason }}）
            </div>
            <div v-else class="reservation-info">（可用）</div>
          </div>
        </div>
        <div v-else class="no-data">请选择查询日期</div>
      </div>
      <template #footer>
        <el-button @click="timeDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑弹窗（仅管理员可见） -->
    <el-dialog 
      :title="dialogTitle" 
      v-model="dialogVisible" 
      width="500px"
      v-if="isAdmin"
    >
      <el-form :model="form" label-width="100px" :rules="formRules" ref="formRef">
        <el-form-item label="教学楼" prop="buildingId">
          <el-select v-model="form.buildingId" placeholder="请选择教学楼">
            <el-option 
              v-for="building in buildingList" 
              :key="building.buildingId"
              :label="building.buildingName"
              :value="building.buildingId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="教室号" prop="classroomId">
          <el-input v-model="form.classroomId" placeholder="例如：101" />
        </el-form-item>
        <el-form-item label="教室名称" prop="classroomName">
          <el-input v-model="form.classroomName" placeholder="例如：J1-101多媒体教室" />
        </el-form-item>
        <el-form-item label="座位数" prop="seatCount">
          <el-input v-model.number="form.seatCount" type="number" min="1" />
        </el-form-item>
        <el-form-item label="设备情况" prop="multimediaType">
          <el-input v-model="form.multimediaType" placeholder="例如：4K投影仪+智能黑板" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'
import { onMounted, reactive, toRefs, ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'ClassroomManager',
  setup() {
    const formRef = ref(null)

    // 响应式数据
    const state = reactive({
      identity: '',
      allClassrooms: [], // 存储所有教室数据
      filteredClassroomList: [], // 筛选后的列表
      buildingList: [],
      loading: false,
      pageNum: 1,
      pageSize: 10,
      total: 0,
      dialogVisible: false,
      timeDialogVisible: false,
      dialogTitle: '新增教室',
      currentClassroom: null,
      reservationDetails: [], // 存储时段预约详情
      timeSlotOptions: [ // 系统默认时段
        "08:00-10:00", "10:00-12:00", "14:00-16:00", 
        "16:00-18:00", "18:00-20:00", "20:00-22:00"
      ],
      selectedDate: null,
      availableTimes: [],
      filterForm: {
        queryDate: new Date(), // 默认当前日期
        buildingId: ''
      },
      form: {
        id: null,
        buildingId: '',
        classroomId: '',
        classroomName: '',
        seatCount: 30,
        multimediaType: '',
        status: 1,
        date: ''
      },
      formRules: {
        buildingId: [{ required: true, message: '请选择教学楼', trigger: 'blur' }],
        classroomId: [{ required: true, message: '请输入教室号', trigger: 'blur' }],
        classroomName: [{ required: true, message: '请输入教室名称', trigger: 'blur' }],
        seatCount: [{ required: true, message: '请输入座位数', trigger: 'blur' }],
        multimediaType: [{ required: true, message: '请输入设备情况', trigger: 'blur' }]
      }
    })

    // 权限判断
    const hasPermission = computed(() => {
      return ['admin', 'manager'].includes(state.identity)
    })
    const isAdmin = computed(() => {
      return state.identity === 'admin'
    })

    // 禁用过去的日期
    const disabledDate = (date) => {
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      return date < today;
    }

    // 获取身份信息
    const getIdentity = async () => {
      try {
        const res = await request.get('/main/loadIdentity')
        if (res.code === '0') {
          state.identity = res.data
          if (hasPermission.value) {
            await Promise.all([
              getBuildingList(),
              getAllClassrooms()
            ])
          }
        } else {
          ElMessage.error('获取身份失败：' + res.msg);
        }
      } catch (err) {
        ElMessage.error('身份验证失败：' + err.message);
      }
    }

    // 获取教学楼列表
    const getBuildingList = async () => {
      try {
        const res = await request.get('/building/listAll')
        if (res.code === '0') {
          state.buildingList = res.data || [];
        } else {
          ElMessage.error('获取教学楼列表失败：' + res.msg);
        }
      } catch (err) {
        ElMessage.error('教学楼接口调用失败：' + err.message);
      }
    }

    // 加载所有教室数据
    const getAllClassrooms = async () => {
      state.loading = true
      try {
        const res = await request.get('/classroom/admin/listAll');
        if (res.code === '0') {
          state.allClassrooms = res.data || [];
          filterClassrooms();
        } else {
          ElMessage.error('查询教室失败：' + res.msg);
          state.allClassrooms = [];
        }
      } catch (err) {
        ElMessage.error('接口调用失败：' + err.message);
        state.allClassrooms = [];
      } finally {
        state.loading = false
      }
    }

    // 筛选教室数据
    const filterClassrooms = () => {
      const targetDate = state.filterForm.queryDate 
        ? new Date(state.filterForm.queryDate).toISOString().split('T')[0]
        : '';

      let filtered = state.allClassrooms.filter(item => item.date === targetDate);
      
      if (state.filterForm.buildingId) {
        filtered = filtered.filter(item => item.buildingId === state.filterForm.buildingId);
      }

      state.total = filtered.length;
      const startIndex = (state.pageNum - 1) * state.pageSize;
      const endIndex = startIndex + state.pageSize;
      state.filteredClassroomList = filtered.slice(startIndex, endIndex);
    }

    // 查看时段详情
    const viewAvailableTime = (classroom) => {
      state.currentClassroom = classroom;
      state.selectedDate = classroom.date;
      state.timeDialogVisible = true;
      fetchAvailableTime();
    }

    // 根据时段获取预约记录
    const getReservationBySlot = (slot) => {
      const startTime = slot.split('-')[0];
      return state.reservationDetails.find(item => item.startTime === startTime);
    }

    // 获取时段预约详情
    const fetchAvailableTime = async () => {
      if (!state.currentClassroom || !state.selectedDate) return;
      try {
        // 管理员查询可用时段，宿管查询全量预约详情
        const url = isAdmin.value 
          ? "/classroom/availableTimeSlots" 
          : "/classroom/reservation/manager/classroomDateReservations";
        
        const res = await request.get(url, {
          params: {
            classroomId: state.currentClassroom.classroomId,
            buildingId: state.currentClassroom.buildingId,
            date: state.selectedDate
          }
        });

        if (res.code === "0") {
          if (isAdmin.value) {
            state.availableTimes = res.data || [];
          } else {
            state.reservationDetails = res.data || [];
          }
        } else {
          ElMessage.error(res.msg || "获取数据失败");
          isAdmin.value ? state.availableTimes = [] : state.reservationDetails = [];
        }
      } catch (err) {
        ElMessage.error("网络错误：" + err.message);
        isAdmin.value ? state.availableTimes = [] : state.reservationDetails = [];
      }
    };

    // 管理员：修改教室状态
    const handleStatusChange = async (row) => {
      if (!isAdmin.value) return;
      try {
        const res = await request.put('/classroom/admin/update', row);
        if (res.code !== '0') {
          ElMessage.error('状态修改失败：' + res.msg);
          row.status = row.status === 1 ? 0 : 1; // 回滚
        } else {
          ElMessage.success(`状态已更新为${row.status === 1 ? '可用' : '禁用'}`);
          const index = state.allClassrooms.findIndex(item => item.id === row.id);
          if (index !== -1) state.allClassrooms[index].status = row.status;
        }
      } catch (err) {
        ElMessage.error('状态修改失败：' + err.message);
        row.status = row.status === 1 ? 0 : 1;
      }
    }

    // 管理员：打开新增弹窗
    const openAddDialog = () => {
      state.dialogTitle = '新增教室';
      const currentDate = new Date(state.filterForm.queryDate).toISOString().split('T')[0];
      state.form = {
        id: null,
        buildingId: state.buildingList[0]?.buildingId || '',
        classroomId: '',
        classroomName: '',
        seatCount: 30,
        multimediaType: '',
        status: 1,
        date: currentDate
      };
      state.dialogVisible = true;
      formRef.value?.clearValidate();
    }

    // 管理员：打开编辑弹窗
    const openEditDialog = (row) => {
      state.dialogTitle = '编辑教室信息';
      state.form = { ...row };
      state.dialogVisible = true;
      formRef.value?.clearValidate();
    }

    // 管理员：提交表单（新增/编辑）
    const handleSubmit = async () => {
      if (!isAdmin.value) return;
      await formRef.value.validate().catch(err => {
        ElMessage.error('表单填写不完整，请检查必填项');
        return;
      });

      try {
        if (state.form.id) {
          // 编辑操作
          const res = await request.put('/classroom/admin/update', state.form);
          if (res.code === '0') {
            ElMessage.success('编辑成功');
            state.dialogVisible = false;
            await getAllClassrooms();
          } else {
            ElMessage.error('编辑失败：' + res.msg);
          }
        } else {
          // 新增操作
          const res = await request.post('/classroom/admin/add', state.form);
          if (res.code === '0') {
            ElMessage.success('新增成功');
            state.dialogVisible = false;
            await getAllClassrooms();
          } else {
            ElMessage.error('新增失败：' + res.msg);
          }
        }
      } catch (err) {
        ElMessage.error('提交失败：' + err.message);
      }
    }

    // 管理员：删除教室
    const handleDelete = async (id) => {
      if (!isAdmin.value) return;
      ElMessageBox.confirm('确定要删除该教室吗？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await request.delete(`/classroom/admin/delete/${id}`);
          if (res.code === '0') {
            ElMessage.success('删除成功');
            await getAllClassrooms();
          } else {
            ElMessage.error('删除失败：' + res.msg);
          }
        } catch (err) {
          ElMessage.error('删除失败：' + err.message);
        }
      }).catch(() => {
        ElMessage.info('已取消删除');
      });
    }

    // 分页和刷新处理
    const handleRefresh = () => {
      state.filterForm = { queryDate: new Date(), buildingId: '' };
      state.pageNum = 1;
      getAllClassrooms();
    }

    const handleDateChange = () => { state.pageNum = 1; }
    const handleSizeChange = (val) => {
      state.pageSize = val;
      state.pageNum = 1;
      filterClassrooms();
    }
    const handleCurrentChange = (val) => {
      state.pageNum = val;
      filterClassrooms();
    }

    // 初始化
    onMounted(() => {
      getIdentity();
    })

    return {
      ...toRefs(state),
      formRef,
      hasPermission,
      isAdmin,
      disabledDate,
      filterClassrooms,
      viewAvailableTime,
      fetchAvailableTime,
      getReservationBySlot,
      handleStatusChange,
      openAddDialog,
      openEditDialog,
      handleSubmit,
      handleDelete,
      handleRefresh,
      handleDateChange,
      handleSizeChange,
      handleCurrentChange
    }
  }
}
</script>

<style scoped>
.container {
  padding: 20px;
}

.button-group {
  margin-bottom: 15px;
}

.filter-form {
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.time-slots {
  margin-top: 15px;
}

.time-slot-item {
  margin: 8px 0;
  display: flex;
  align-items: center;
  flex-wrap: wrap; /* 支持换行 */
}

.reservation-info {
  font-size: 14px;
  margin-left: 10px;
  color: #666;
  word-break: break-all; /* 长文本自动换行 */
}

.no-data {
  margin-top: 15px;
  color: #666;
  text-align: center;
}

.dialog-content {
  margin-bottom: 10px;
}
</style>