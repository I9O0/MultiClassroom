<template>
  <el-card style="margin: 15px; min-height: calc(100vh - 80px)">
    <div class="page-title">教室查询与预约</div>
    
    <!-- 查询条件区域 -->
    <el-form :inline="true" class="search-form" ref="searchForm">
      <el-form-item>
        <el-select 
          v-model="selectedBuilding" 
          placeholder="选择教学楼" 
          clearable
          style="width: 180px; margin-right: 10px"
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
        <el-date-picker
          v-model="selectedDate"
          type="date"
          placeholder="选择预约日期"
          :disabled-date="disabledDate"
          @change="handleDateChange"
          style="margin-right: 10px"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="getClassrooms">查询可用教室</el-button>
      </el-form-item>
    </el-form>
    
    <!-- 教室列表 -->
    <el-table 
      :data="classroomList" 
      border 
      style="width: 100%; margin-top: 20px"
      v-loading="classroomLoading"
    >
      <el-table-column prop="date" label="日期" width="120"></el-table-column>
      <el-table-column prop="classroomId" label="教室号" width="100"></el-table-column>
      <el-table-column prop="classroomName" label="教室名称"></el-table-column>
      <el-table-column prop="seatCount" label="容纳人数" width="100"></el-table-column>
      <el-table-column prop="multimediaType" label="设备情况"></el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '可用' : '维护中' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button 
            type="primary" 
            size="small" 
            @click="handleReservation(scope.row)"
            :disabled="scope.row.status !== 1 || !isDateValid(scope.row.date)"
          >
            预约
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 我的预约记录 -->
    <div class="reservation-record">
      <el-collapse>
        <el-collapse-item title="我的预约记录" name="1">
          <el-table 
            :data="myReservations" 
            border 
            style="width: 100%; margin-top: 10px"
            v-loading="reservationLoading"
          >
            <el-table-column prop="classroomId" label="教室号" width="100"></el-table-column>
            <el-table-column prop="reserveDate" label="预约日期" width="120">
              <template #default="scope">
                {{ formatDate(scope.row.reserveDate) }}
              </template>
            </el-table-column>
            <el-table-column prop="startTime" label="开始时间" width="100"></el-table-column>
            <el-table-column prop="endTime" label="结束时间" width="100"></el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusTagType(scope.row.status)">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="auditor" label="审批人" align="center" width="120">
              <template #default="scope">
                {{ scope.row.auditor || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="adminRemark" label="审批意见" align="center" min-width="200">
              <template #default="scope">
                {{ scope.row.adminRemark || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <!-- 取消按钮 -->
                <el-button 
                  type="danger" 
                  size="small" 
                  @click="cancelReservation(scope.row.id)"
                  :disabled="!canCancel(scope.row)"
                >
                  取消
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-collapse-item>
      </el-collapse>
    </div>

    <!-- 预约弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      title="教室预约" 
      width="400px"
      :before-close="handleDialogClose"
    >
      <el-form :model="reservationForm" label-width="100px">
        <el-form-item label="教室">
          <el-input v-model="reservationForm.classroomName" disabled></el-input>
        </el-form-item>
        <el-form-item label="预约日期">
          <el-input v-model="reservationForm.reserveDate" disabled></el-input>
        </el-form-item>
        <el-form-item label="可用时段" required>
          <el-select 
            v-model="reservationForm.startTime" 
            @change="handleTimeSlotChange"
            placeholder="选择开始时间"
          >
            <el-option 
              v-for="slot in availableTimeSlots" 
              :key="slot" 
              :label="slot" 
              :value="slot.split('-')[0]"
            ></el-option>
          </el-select>
          <span style="margin: 0 10px;">至</span>
          <el-input 
            v-model="reservationForm.endTime" 
            disabled 
            style="width: 100px"
          ></el-input>
        </el-form-item>
        <el-form-item label="预约理由" required>
          <el-input 
            v-model="reservationForm.reserveReason" 
            type="textarea" 
            rows="3"
            placeholder="请输入至少10个字的预约理由"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReservation">确认提交</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script>
import request from "@/utils/request";
import { ElMessage } from "element-plus";

export default {
  name: "ClassroomQuery",
  data() {
    return {
      classroomList: [],
      myReservations: [],
      buildingList: [],
      selectedBuilding: "",
      selectedDate: this.getDateZeroTime(new Date()),
      identity: "",
      classroomLoading: false,
      reservationLoading: false,
      currentQueryDate: "",
      dialogVisible: false,
      availableTimeSlots: [],
      reservationForm: {
        classroomId: "",
        buildingId: "",
        classroomName: "",
        reserveDate: "",
        startTime: "",
        endTime: "",
        reserveReason: ""
      }
    };
  },
  created() {
    const storedIdentity = sessionStorage.getItem("identity");
    this.identity = storedIdentity ? JSON.parse(storedIdentity) : "stu";
    
    this.getBuildingList();
    this.currentQueryDate = this.formatDate(this.selectedDate);
    this.getClassrooms();
    this.getMyReservations();
  },
  methods: {
    // 状态文本映射
    getStatusText(status) {
      if (status === 0) return '待审核';
      if (status === 1) return '已通过';
      if (status === 2) return '已拒绝';
      return '未知';
    },
    
    // 状态标签类型
    getStatusTagType(status) {
      if (status === 0) return 'info';
      if (status === 1) return 'success';
      if (status === 2) return 'danger';
      return 'info';
    },

    // 判断是否可以取消预约
    canCancel(reservation) {
      // 待审核状态可以取消
      if (reservation.status === 0) {
        return true;
      }
      
      // 已通过但未到预约时间可以取消（简化逻辑，移除签到相关判断）
      if (reservation.status === 1) {
        const now = new Date();
        const startTime = new Date(`${reservation.reserveDate}T${reservation.startTime}`);
        return now < startTime;
      }
      
      return false;
    },

    // 处理预约弹窗打开
    async handleReservation(classroom) {
      if (!classroom) {
        ElMessage.warning("教室数据异常");
        return;
      }
      
      const reservationDate = this.currentQueryDate;
      if (!this.isDateValid(reservationDate)) {
        ElMessage.warning("只能预约7天内的教室");
        return;
      }

      this.reservationForm = {
        classroomId: classroom.classroomId,
        buildingId: classroom.buildingId,
        classroomName: classroom.classroomName,
        reserveDate: reservationDate,
        startTime: "",
        endTime: "",
        reserveReason: ""
      };

      try {
        const res = await request.get("/classroom/availableTimeSlots", {
          params: {
            classroomId: classroom.classroomId,
            date: reservationDate,
            buildingId: classroom.buildingId
          }
        });
        const allAvailableSlots = res.code === "0" ? res.data : [];
        this.availableTimeSlots = this.filterExpiredTimeSlots(allAvailableSlots);
        
        if (this.availableTimeSlots.length === 0) {
          ElMessage.warning("当前日期已没有可用的预约时段");
          this.dialogVisible = false;
        } else {
          this.dialogVisible = true;
        }
      } catch (err) {
        ElMessage.warning("获取可用时段异常，使用默认时段");
        const defaultSlots = ["08:00-10:00", "10:00-12:00", "14:00-16:00", "16:00-18:00", "18:00-20:00", "20:00-22:00"];
        this.availableTimeSlots = this.filterExpiredTimeSlots(defaultSlots);
        this.dialogVisible = this.availableTimeSlots.length > 0;
      }
    },

    // 获取零点时间的日期
    getDateZeroTime(date) {
      const d = new Date(date);
      d.setHours(0, 0, 0, 0);
      return d;
    },

    // 日期变更处理
    handleDateChange(date) {
      if (date) {
        this.currentQueryDate = this.formatDate(date);
      }
    },

    // 日期格式化
    formatDate(date) {
      if (!date) return "";
      const dateObj = typeof date === 'string' 
        ? new Date(date) 
        : new Date(date);
      
      if (isNaN(dateObj.getTime())) return "";
      
      const year = dateObj.getFullYear();
      const month = String(dateObj.getMonth() + 1).padStart(2, "0");
      const day = String(dateObj.getDate()).padStart(2, "0");
      return `${year}-${month}-${day}`;
    },

    // 禁用日期（只能选今天及未来6天）
    disabledDate(date) {
      const today = this.getDateZeroTime(new Date());
      const sevenDaysLater = new Date(today);
      sevenDaysLater.setDate(today.getDate() + 6);
      return date < today || date > sevenDaysLater;
    },

    // 验证日期是否有效（7天内）
    isDateValid(dateStr) {
      const reg = /^\d{4}-\d{2}-\d{2}$/;
      if (!reg.test(dateStr)) return false;
      const targetDate = new Date(`${dateStr}T00:00:00`);
      if (isNaN(targetDate.getTime())) return false;
      const today = this.getDateZeroTime(new Date());
      const sevenDaysLater = new Date(today);
      sevenDaysLater.setDate(today.getDate() + 6);
      return targetDate >= today && targetDate <= sevenDaysLater;
    },

    // 过滤已过期的时段
    filterExpiredTimeSlots(slots) {
      const currentDate = this.formatDate(new Date());
      const isToday = this.currentQueryDate === currentDate;
      
      if (!isToday) {
        return slots;
      }
      
      const now = new Date();
      const currentHour = now.getHours();
      const currentMinute = now.getMinutes();
      const currentTotalMinutes = currentHour * 60 + currentMinute;
      
      return slots.filter(slot => {
        const startTimeStr = slot.split('-')[0];
        const [startHour, startMinute] = startTimeStr.split(':').map(Number);
        const slotTotalMinutes = startHour * 60 + startMinute;
        return slotTotalMinutes - currentTotalMinutes >= 30;
      });
    },

    // 获取教学楼列表
    async getBuildingList() {
      try {
        const res = await request.get("/building/listAll");
        let allBuildings = res.code === "0" ? res.data || [] : [];
        if (this.identity === "stu") {
          this.buildingList = allBuildings.filter(building => 
            ["J1", "J2"].includes(building.buildingId)
          );
        } else {
          this.buildingList = allBuildings.filter(building => 
            ["J3", "J4"].includes(building.buildingId)
          );
        }
        if (this.buildingList.length > 0) {
          this.selectedBuilding = this.buildingList[0].buildingId;
        }
      } catch (err) {
        ElMessage.error("获取教学楼列表异常：" + err.message);
      }
    },

    // 查询可用教室
    async getClassrooms() {
      if (!this.selectedDate) {
        ElMessage.warning("请选择预约日期");
        return;
      }
      if (!this.selectedBuilding) {
        ElMessage.warning("请选择教学楼");
        return;
      }
      
      this.classroomLoading = true;
      try {
        this.currentQueryDate = this.formatDate(this.selectedDate);
        const res = await request.get("/classroom/list", { 
          params: { buildingId: this.selectedBuilding } 
        });
        const allClassrooms = res.code === "0" ? res.data || [] : [];
        
        this.classroomList = allClassrooms.filter(classroom => 
          classroom.date === this.currentQueryDate
        );
      } catch (err) {
        ElMessage.error("查询教室失败：" + err.message);
        this.classroomList = [];
      } finally {
        this.classroomLoading = false;
      }
    },

    // 时段选择变更处理
    handleTimeSlotChange(startTime) {
      const selectedSlot = this.availableTimeSlots.find(slot => slot.startsWith(startTime));
      this.reservationForm.endTime = selectedSlot ? selectedSlot.split('-')[1] : "";
    },

    // 提交预约
    async submitReservation() {
      const { classroomId, buildingId, startTime, endTime, reserveDate, reserveReason } = this.reservationForm;
      
      if (!classroomId || !buildingId) {
        ElMessage.error("教室信息异常，请重新选择教室");
        return;
      }
      if (!startTime || !endTime) {
        ElMessage.warning("请选择完整时段");
        return;
      }
      if (reserveReason.trim().length < 10) {
        ElMessage.warning("预约理由至少需要10个字");
        return;
      }

      try {
        const user = JSON.parse(sessionStorage.getItem("user"));
        if (!user?.username) {
          ElMessage.warning("请先登录");
          return;
        }

        const res = await request.post("/classroom/reservation/submit", {
          username: user.username,
          studentName: user.name,
          buildingId: buildingId,
          classroomId: classroomId,
          reserveDate: reserveDate,
          startTime: startTime,
          endTime: endTime,
          reserveReason: reserveReason.trim()
        });

        if (res.code === "0") {
          ElMessage.success("预约申请已提交，请等待审核");
          this.dialogVisible = false;
          this.getMyReservations();
        } else {
          ElMessage.error(res.msg || "预约提交失败");
        }
      } catch (err) {
        ElMessage.error("提交预约失败：" + err.message);
      }
    },

    handleDialogClose() {
      this.reservationForm = {
        classroomId: "",
        buildingId: "",
        classroomName: "",
        reserveDate: "",
        startTime: "",
        endTime: "",
        reserveReason: ""
      };
      this.availableTimeSlots = [];
      this.dialogVisible = false;
    },

    async getMyReservations() {
      this.reservationLoading = true;
      try {
        const user = JSON.parse(sessionStorage.getItem("user"));
        if (!user?.username) {
          ElMessage.warning("请先登录");
          return;
        }
        const res = await request.get("/classroom/reservation/myList", {
          params: { username: user.username }
        });
        if (res.code === "0") {
          this.myReservations = (res.data || []).map(item => ({
            ...item,
            reserveDate: this.formatDate(item.reserveDate)
          }));
        } else {
          this.myReservations = [];
          ElMessage.error(res.msg || "查询预约记录失败");
        }
      } catch (err) {
        ElMessage.error("查询预约记录失败：" + err.message);
        this.myReservations = [];
      } finally {
        this.reservationLoading = false;
      }
    },

    async cancelReservation(id) {
      try {
        const user = JSON.parse(sessionStorage.getItem("user"));
        if (!user?.username) {
          ElMessage.warning("请先登录");
          return;
        }
        const res = await request.delete(`/classroom/reservation/cancel/${id}`, {
          params: { username: user.username }
        });
        if (res.code === "0") {
          ElMessage.success("取消预约成功");
          this.getMyReservations();
        } else {
          ElMessage.error(res.msg || "取消预约失败");
        }
      } catch (err) {
        ElMessage.error("取消预约失败：" + err.message);
      }
    }
  }
};
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
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.reservation-record {
  margin-top: 30px;
}
</style>