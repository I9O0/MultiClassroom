<template>
  <div class="container">
    <!-- 权限校验验：允许管理员和宿管访问 -->
    <div v-if="!['admin', 'manager'].includes(identity)">
      <el-alert title="无权限访问" type="error" show-icon />
    </div>

    <div v-else>
      <!-- 操作按钮区 - 宿管无新增/删除权限 -->
      <div class="button-group" v-if="identity === 'admin'">
        <el-button type="primary" @click="openAddDialog">新增教室</el-button>
        <el-button type="warning" @click="handleRefresh">刷新</el-button>
      </div>
      <div class="button-group" v-else>
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
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
              active-text="可用"
              inactive-text="禁用"
              :disabled="identity !== 'admin'"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small" 
              @click="openEditDialog(scope.row)"
              v-if="identity === 'admin'"
            >
              编辑信息
            </el-button>
            <el-button 
              type="info" 
              size="small" 
              @click="viewReservations(scope.row)"
              style="margin-left: 5px"
            >
              查看预约
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleDelete(scope.row.id)"
              style="margin-left: 5px"
              v-if="identity === 'admin'"
            >
              删除
            </el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog 
      :title="dialogTitle" 
      v-model="dialogVisible" 
      width="500px"
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

    <!-- 预约详情弹窗 -->
    <el-dialog 
      title="教室预约详情" 
      v-model="reservationDialogVisible" 
      width="600px"
    >
      <div v-if="loadingReservations" class="loading-container">
        <el-loading-spinner size="large" />
        <p>加载中...</p>
      </div>
      
      <div v-else-if="reservationList.length === 0" class="empty-state">
        <el-empty description="该教室当天暂无预约记录" />
      </div>
      
      <el-table 
        v-else
        :data="reservationList" 
        border 
        stripe 
        style="width: 100%"
      >
        <el-table-column prop="reserverName" label="预约人" />
        <el-table-column prop="startTime" label="开始时间" />
        <el-table-column prop="endTime" label="结束时间" />
        <el-table-column prop="reserveReason" label="预约理由" />
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
              {{ scope.row.status === 1 ? '已通过' : '待审核' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" />
      </el-table>

      <template #footer>
        <el-button @click="reservationDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'
import { onMounted, reactive, toRefs, ref } from 'vue'
import { ElMessage, ElMessageBox, ElEmpty } from 'element-plus'

export default {
  name: 'ClassroomManager',
  components: { ElEmpty },
  setup() {
    const formRef = ref(null)

    // 响应式数据
    const state = reactive({
      identity: '',
      allClassrooms: [], // 存储接口返回的所有数据（含所有日期）
      filteredClassroomList: [], // 按日期+教学楼筛选后的列表
      buildingList: [],
      pageNum: 1,
      pageSize: 10,
      total: 0, // 筛选后的总条数
      dialogVisible: false,
      dialogTitle: '新增教室',
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
        date: '' // 存储当前编辑的日期
      },
      formRules: {
        buildingId: [{ required: true, message: '请选择教学楼', trigger: 'blur' }],
        classroomId: [{ required: true, message: '请输入教室号', trigger: 'blur' }],
        classroomName: [{ required: true, message: '请输入教室名称', trigger: 'blur' }],
        seatCount: [{ required: true, message: '请输入座位数', trigger: 'blur' }],
        multimediaType: [{ required: true, message: '请输入设备情况', trigger: 'blur' }]
      },
      // 新增预约详情相关状态
      reservationDialogVisible: false,
      reservationList: [],
      loadingReservations: false,
    })

    // 禁用过去的日期
    const disabledDate = (date) => {
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      return date < today;
    }

    // 日期改变时重置页码
    const handleDateChange = () => {
      state.pageNum = 1;
    }

    // 获取身份信息
    const getIdentity = async () => {
      try {
        const res = await request.get('/main/loadIdentity')
        if (res.code === '0') {
          state.identity = res.data
          // 管理员和宿管都需要加载教学楼列表和教室数据
          await getBuildingList()
          await getAllClassrooms() // 加载所有数据，前端筛选
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

    // 加载所有教室数据（管理员和宿管都使用同一个接口）
    const getAllClassrooms = async () => {
      try {
        // 调用允许管理员和宿管访问的接口
        const res = await request.get('/classroom/admin/listAll');
        if (res.code === '0') {
          state.allClassrooms = res.data || [];
          filterClassrooms(); // 加载后立即筛选
        } else {
          ElMessage.error('查询教室失败：' + res.msg);
          state.allClassrooms = [];
        }
      } catch (err) {
        ElMessage.error('接口调用失败：' + err.message);
        state.allClassrooms = [];
      }
    }

    // 核心：前端按日期和教学楼筛选数据
    const filterClassrooms = () => {
      // 格式化查询日期为 yyyy-MM-dd
      const targetDate = state.filterForm.queryDate 
        ? new Date(state.filterForm.queryDate).toISOString().split('T')[0]
        : '';

      // 1. 按日期筛选（只保留当前查询日期的数据）
      let filtered = state.allClassrooms.filter(item => {
        // 后端返回的date格式是 yyyy-MM-dd（如"2025-11-07"）
        return item.date === targetDate;
      });

      // 2. 按教学楼筛选（如果选择了教学楼）
      if (state.filterForm.buildingId) {
        filtered = filtered.filter(item => {
          return item.buildingId === state.filterForm.buildingId;
        });
      }

      // 3. 处理分页
      state.total = filtered.length;
      const startIndex = (state.pageNum - 1) * state.pageSize;
      const endIndex = startIndex + state.pageSize;
      state.filteredClassroomList = filtered.slice(startIndex, endIndex);
    }

    // 切换状态并保存（只有管理员有权限）
    const handleStatusChange = async (row) => {
      // 如果不是管理员，直接返回
      if (state.identity !== 'admin') {
        ElMessage.warning('只有管理员可以修改状态');
        // 状态回滚
        row.status = row.status === 1 ? 0 : 1;
        return;
      }
      
      try {
        // 调用后端修改状态接口
        const res = await request.put('/classroom/admin/update', {
          ...row, // 保留原有字段
          status: row.status // 更新状态
        });
        if (res.code !== '0') {
          ElMessage.error('状态修改失败：' + res.msg);
          row.status = row.status === 1 ? 0 : 1; // 回滚
        } else {
          ElMessage.success(`状态已更新为${row.status === 1 ? '可用' : '禁用'}`);
          // 更新本地全量数据，保持同步
          const index = state.allClassrooms.findIndex(item => item.id === row.id);
          if (index !== -1) {
            state.allClassrooms[index].status = row.status;
          }
        }
      } catch (err) {
        ElMessage.error('状态修改失败：' + err.message);
        row.status = row.status === 1 ? 0 : 1; // 回滚
      }
    }

    // 打开新增弹窗（只有管理员可以访问）
    const openAddDialog = () => {
      state.dialogTitle = '新增教室';
      // 新增时默认关联当前查询日期
      const currentDate = new Date(state.filterForm.queryDate).toISOString().split('T')[0];
      state.form = {
        id: null,
        buildingId: state.buildingList.length > 0 ? state.buildingList[0].buildingId : '',
        classroomId: '',
        classroomName: '',
        seatCount: 30,
        multimediaType: '',
        status: 1,
        date: currentDate
      };
      state.dialogVisible = true;
      if (formRef.value) formRef.value.clearValidate();
    }

    // 打开编辑弹窗（只有管理员可以访问）
    const openEditDialog = (row) => {
      state.dialogTitle = '编辑教室信息';
      state.form = { ...row }; // 复制完整数据（含date）
      state.dialogVisible = true;
      if (formRef.value) formRef.value.clearValidate();
    }

    // 提交新增/编辑（只有管理员可以操作）
    const handleSubmit = async () => {
      // 如果不是管理员，直接返回
      if (state.identity !== 'admin') {
        ElMessage.warning('只有管理员可以执行此操作');
        return;
      }
      
      await formRef.value.validate().catch(err => {
        ElMessage.error('表单填写不完整，请检查必填项');
        return;
      });

      try {
        if (state.form.id) {
          // 编辑
          const res = await request.put('/classroom/admin/update', state.form);
          if (res.code === '0') {
            ElMessage.success('编辑成功');
            state.dialogVisible = false;
            await getAllClassrooms(); // 重新加载全量数据
          } else {
            ElMessage.error('编辑失败：' + res.msg);
          }
        } else {
          // 新增
          const res = await request.post('/classroom/admin/add', state.form);
          if (res.code === '0') {
            ElMessage.success('新增成功');
            state.dialogVisible = false;
            await getAllClassrooms(); // 重新加载全量数据
          } else {
            ElMessage.error('新增失败：' + res.msg);
          }
        }
      } catch (err) {
        ElMessage.error('提交失败：' + err.message);
      }
    }

    // 删除教室（只有管理员可以操作）
    const handleDelete = async (id) => {
      // 如果不是管理员，直接返回
      if (state.identity !== 'admin') {
        ElMessage.warning('只有管理员可以执行此操作');
        return;
      }
      
      ElMessageBox.confirm('确定要删除该教室吗？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await request.delete(`/classroom/admin/delete/${id}`);
          if (res.code === '0') {
            ElMessage.success('删除成功');
            await getAllClassrooms(); // 重新加载
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

    // 刷新
    const handleRefresh = () => {
      state.filterForm = {
        queryDate: new Date(),
        buildingId: ''
      };
      state.pageNum = 1;
      getAllClassrooms();
    }

    // 分页
    const handleSizeChange = (val) => {
      state.pageSize = val;
      state.pageNum = 1;
      filterClassrooms(); // 基于筛选后的数据分页
    }

    const handleCurrentChange = (val) => {
      state.pageNum = val;
      filterClassrooms(); // 基于筛选后的数据分页
    }

    // 查看教室预约详情（根据身份调用不同接口）
    const viewReservations = async (row) => {
      state.reservationDialogVisible = true;
      state.loadingReservations = true;
      state.reservationList = [];
      
      try {
        // 格式化日期为 yyyy-MM-dd
        const dateStr = new Date(state.filterForm.queryDate).toISOString().split('T')[0];
        let res;
        
        // 根据身份选择不同接口
        if (state.identity === 'admin') {
          // 管理员接口
          res = await request.get('/classroom/reservation/admin/classroomDateReservations', {
            params: {
              classroomId: row.classroomId,
              date: dateStr
            }
          });
        } else {
          // 宿管接口（需传递buildingId）
          res = await request.get('/classroom/reservation/manager/classroomDateReservations', {
            params: {
              classroomId: row.classroomId,
              buildingId: row.buildingId, // 补充教学楼ID参数
              date: dateStr
            }
          });
        }
        
        if (res.code === '0') {
          state.reservationList = res.data || [];
        } else {
          ElMessage.error('查询预约失败：' + res.msg);
        }
      } catch (err) {
        ElMessage.error('接口调用失败：' + err.message);
      } finally {
        state.loadingReservations = false;
      }
    }

    // 初始化
    onMounted(() => {
      getIdentity();
    })

    return {
      ...toRefs(state),
      formRef,
      disabledDate,
      handleDateChange,
      getIdentity,
      getBuildingList,
      getAllClassrooms,
      filterClassrooms,
      handleStatusChange,
      openAddDialog,
      openEditDialog,
      handleSubmit,
      handleDelete,
      handleRefresh,
      handleSizeChange,
      handleCurrentChange,
      viewReservations
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

.loading-container {
  text-align: center;
  padding: 30px 0;
}

.empty-state {
  padding: 30px 0;
}
</style>