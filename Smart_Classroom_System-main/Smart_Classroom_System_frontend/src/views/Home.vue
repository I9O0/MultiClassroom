<template>
  <el-card style="margin: 15px; min-height: calc(100vh - 80px); padding: 20px;">
    <!-- 头部核心数据卡片（不变，仅接口对接调整） -->
    <div class="header-cards">
      <el-row :gutter="20">
        <!-- 总教室数量（实际存在数，对接 /classroom/total，仅admin/manager可见） -->
        <el-col :span="6">
          <div class="card-item">
            <div class="card-tag">实时</div>
            <div class="card-title">总教室数量</div>
            <div class="card-value">{{ classroomTotal || "加载中..." }}</div>
            <div class="card-desc">实际存在的可用教室总数</div>
          </div>
        </el-col>
        <!-- 待处理报修数（不变） -->
        <el-col :span="6">
          <div class="card-item">
            <div class="card-tag">实时</div>
            <div class="card-title">待处理报修数</div>
            <div class="card-value">{{ pendingRepairNum || "加载中..." }}</div>
            <div class="card-desc">需处理的设备故障报修</div>
          </div>
        </el-col>
        <!-- 今日预约次数（不变） -->
        <el-col :span="6">
          <div class="card-item">
            <div class="card-tag">实时</div>
            <div class="card-title">今日预约次数</div>
            <div class="card-value">{{ todayReservationNum || "加载中..." }}</div>
            <div class="card-desc">含待审核及已通过预约</div>
          </div>
        </el-col>
        <!-- 当日可用教室数（对接新接口 /available/today） -->
        <el-col :span="6">
          <div class="card-item">
            <div class="card-tag">当日</div>
            <div class="card-title">当日可用教室数</div>
            <div class="card-value">{{ currentAvailableNum || "加载中..." }}</div>
            <div class="card-desc">今日至少1个时段可预约的教室数（去重）</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 下部内容区域（图表对接新接口 /available/building） -->
    <div class="content-area" style="margin-top: 40px;">
      <el-row :gutter="30">
        <!-- 左侧：教室公告（不变） -->
        <el-col :span="6">
          <div class="content-title">教室公告</div>
          <el-timeline class="notice-timeline">
            <el-timeline-item 
              v-for="(item, index) in activities.slice(0, 8)" 
              :key="index"
              :timestamp="formatTime(item.releaseTime)"
              timestamp-placement="top"
            >
              <div class="notice-title">{{ item.title }}</div>
            </el-timeline-item>
          </el-timeline>
        </el-col>

        <!-- 中部：当日有可用时段的教室分布（对接新接口） -->
        <el-col :span="12">
          <div class="content-title">当日有可用时段的教室分布</div>
          <div style="height: 500px; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.1);">
            <div ref="chartRef" style="width: 100%; height: 100%;"></div>
          </div>
        </el-col>

        <!-- 右侧：天气+日历（不变） -->
        <el-col :span="6">
          <div class="content-title">环境&日程</div>
          <div class="weather-box" style="margin-bottom: 20px;">
            <weather />
          </div>
          <el-card style="border-radius: 8px;">
            <Calender />
          </el-card>
        </el-col>
      </el-row>
    </div>
  </el-card>
</template>

<script>
import weather from "@/components/weather";
import Calender from "@/components/Calendar";
import * as echarts from 'echarts';
import request from "@/utils/request";
import { ElMessage } from 'element-plus';

export default {
  name: "Home",
  components: {
    weather,
    Calender
  },
  data() {
    return {
      // 核心数据
      classroomTotal: "",     // 实际存在的可用教室总数（去重）
      pendingRepairNum: "",   // 待处理报修数
      todayReservationNum: "",// 今日预约次数
      currentAvailableNum: "",// 当日可用教室数（日期+去重+排除完全预约）
      activities: [],          // 教室公告
      // 新增：当日日期（格式：YYYY-MM-DD），用于给后端传参
      today: new Date().toISOString().split('T')[0],
      // 图表相关
      chartInstance: null,    
      chartData: []           
    };
  },
  mounted() {
    this.initChart();
    window.addEventListener("resize", this.resizeChart);
  },
  beforeUnmount() {
    window.removeEventListener("resize", this.resizeChart);
    this.chartInstance?.dispose();
  },
  created() {
    this.loadAllData(); // 加载所有数据（对接新接口）
    this.getTodayAvailableBuildingData(); // 加载图表数据（对接新接口）
  },
  methods: {
    // 批量加载数据：对接后端新接口，传递当日日期参数
    async loadAllData() {
  try {
    const [
      totalRes,
      repairRes,
      reservationRes,
      availableRes,
      noticeRes
    ] = await Promise.all([
      request.get("/classroom/total"),                  // 实际存在数（去重+admin/manager权限）
      request.get("/repair/classroom/countByStatus"),   // 报修统计（不变）
      request.get("/classroom/reservation/todayCount"), // 今日预约（不变）
      request.get(`/classroom/available/today?date=${this.today}`),
      request.get("/notice/homePageNotice")             // 公告（不变）
    ]);

    // 实际存在的可用教室总数
    if (totalRes.code === "0") this.classroomTotal = totalRes.data;
    else if (totalRes.code === "-1") ElMessage.warning(totalRes.msg); // 无权限提示
    else ElMessage.error("获取总教室数量失败：" + totalRes.msg);

    // 待处理报修数（修改部分）
    if (repairRes.code === "0") {
      const pendingCount = repairRes.data.pending || 0;
      this.pendingRepairNum = pendingCount > 0 ? pendingCount : "暂无报修";
    } else {
      ElMessage.error("获取报修数据失败：" + repairRes.msg);
    }

    // 今日预约次数（修改部分）
    if (reservationRes.code === "0") {
      const reservationCount = reservationRes.data || 0;
      this.todayReservationNum = reservationCount > 0 ? reservationCount : "暂无预约";
    } else {
      ElMessage.error("获取预约数据失败：" + reservationRes.msg);
    }

    // 当日可用教室数
    if (availableRes.code === "0") this.currentAvailableNum = availableRes.data;
    else if (availableRes.code === "-1") ElMessage.warning(availableRes.msg); // 未登录提示
    else ElMessage.error("获取当日可用教室数失败：" + availableRes.msg);

    // 公告数据
    if (noticeRes.code === "0") this.activities = noticeRes.data;
    else ElMessage.error("获取公告失败：" + noticeRes.msg);

  } catch (error) {
    ElMessage.error("网络异常，部分数据加载失败");
    console.error("数据加载失败详情：", error);
  }
},

    // 格式化公告时间（不变）
    formatTime(timeStr) {
      if (!timeStr) return "";
      const date = new Date(timeStr);
      return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`;
    },

    // ======================== 图表相关：对接新接口 /available/building ========================
    initChart() {
      const chartDom = this.$refs.chartRef;
      this.chartInstance = echarts.init(chartDom);
      
      const option = {
        title: {
          text: "当日有可用时段的教室分布（去重）",
          left: "center",
          textStyle: { fontSize: 16, color: "#333", fontWeight: 500 }
        },
        tooltip: {
          trigger: "item",
          formatter: "{b}：{c} 间（当日有可用时段）",
          textStyle: { fontSize: 12 }
        },
        legend: {
          orient: "vertical",
          left: "left",
          textStyle: { fontSize: 12 },
          itemWidth: 12,
          itemHeight: 12
        },
        series: [
          {
            name: "当日可用教室数",
            type: "pie",
            radius: ["40%", "70%"],
            data: this.chartData,
            color: ["#409EFF", "#67C23A", "#E6A23C", "#F56C6C"],
            label: {
              fontSize: 12,
              formatter: "{b}：{c} 间",
              padding: [0, 0, 0, -20]
            },
            labelLine: {
              show: this.chartData.length > 0,
              length: 15,
              length2: 20
            }
          }
        ],
        noDataLoadingOption: {
          text: "暂无当日可用教室分布数据",
          textStyle: { fontSize: 14, color: "#999" },
          effect: "bubble",
          effectOption: { effectSize: 10 }
        }
      };

      this.chartInstance.setOption(option);
    },

    // 核心改动2：对接新接口 /available/building，传递 date 参数
    async getTodayAvailableBuildingData() {
      try {
        // 传递当日日期，请求新接口（日期+去重+排除完全预约）
        const res = await request.get(`/classroom/available/building?date=${this.today}`);
        if (res.code === "0" && Array.isArray(res.data) && res.data.length > 0) {
          this.chartData = res.data; // 接收后端返回的去重后楼栋分布
          this.updateChart();
        } else if (res.code === "-1") {
          ElMessage.warning(res.msg); // 未登录提示
          this.useBackupData(); // 显示模拟数据
        } else {
          ElMessage.warning("未查询到当日可用教室分布数据，显示模拟数据");
          this.useBackupData();
        }
      } catch (error) {
        ElMessage.error("获取教室分布数据失败，显示模拟数据");
        this.useBackupData();
      }
    },

    updateChart() {
      this.chartInstance.setOption({
        series: [{ data: this.chartData }],
        legend: { data: this.chartData.map(item => item.name) }
      });
    },

    // 兜底模拟数据（匹配去重后的真实数量：J1=3, J2=6, J3=2, J4=2）
    useBackupData() {
      this.chartData = [
        { name: "J1", value: 3 },
        { name: "J2", value: 6 },
        { name: "J3", value: 2 },
        { name: "J4", value: 2 }
      ];
      this.updateChart();
    },

    resizeChart() {
      this.chartInstance?.resize();
    }
    // ======================== 图表相关方法结束 ========================
  }
};
</script>

<style scoped>
/* 所有样式保持不变，仅调整卡片描述文案（可选） */
.header-cards { margin-bottom: 20px; }
.card-item {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}
.card-item:hover { transform: translateY(-5px); }
.card-tag { font-size: 12px; color: #67C23A; margin-bottom: 8px; }
.card-item:nth-child(4) .card-tag { color: #409EFF; }
.card-title { font-size: 16px; color: #333; margin-bottom: 10px; }
.card-value { font-size: 28px; font-weight: bold; color: #409EFF; margin-bottom: 8px; }
.card-desc { font-size: 12px; color: #999; }

.content-area { width: 100%; }
.content-title {
  font-size: 22px;
  color: #333;
  margin-bottom: 20px;
  padding-left: 10px;
  border-left: 4px solid #409EFF;
}

.notice-timeline {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  height: 500px;
  overflow-y: auto;
}
.notice-timeline::-webkit-scrollbar { width: 6px; }
.notice-timeline::-webkit-scrollbar-thumb { background: #eee; border-radius: 3px; }
.notice-title { font-size: 15px; color: #333; cursor: pointer; }
.notice-title:hover { color: #409EFF; }

.weather-box {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

::v-deep .echarts-container {
  width: 100%;
  height: 100%;
}
</style>