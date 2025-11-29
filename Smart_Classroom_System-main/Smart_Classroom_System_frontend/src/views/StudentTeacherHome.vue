<template>
  <el-card style="margin: 15px; min-height: calc(100vh - 80px); padding: 20px;">
    <!-- 头部核心数据卡片（仅保留总教室数量+当日可用教室数，调整列宽） -->
    <div class="header-cards">
      <el-row :gutter="20">
        <!-- 总教室数量（调整为span12，占半宽） -->
        <el-col :span="12">
          <div class="card-item">
            <div class="card-tag">实时</div>
            <div class="card-title">总教室数量</div>
            <div class="card-value">{{ classroomTotal || "加载中..." }}</div>
            <div class="card-desc">实际存在的可用教室总数</div>
          </div>
        </el-col>
        <!-- 当日可用教室数（调整为span12，占半宽） -->
        <el-col :span="12">
          <div class="card-item">
            <div class="card-tag">当日</div>
            <div class="card-title">当日可用教室数</div>
            <div class="card-value">{{ currentAvailableNum || "加载中..." }}</div>
            <div class="card-desc">今日至少1个时段可预约的教室数（去重）</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 下部内容区域（保留公告+图表+天气日历，逻辑不变） -->
    <div class="content-area" style="margin-top: 40px;">
      <el-row :gutter="30">
        <!-- 左侧：教室公告 -->
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

        <!-- 中部：当日有可用时段的教室分布 -->
        <el-col :span="12">
          <div class="content-title">当日有可用时段的教室分布</div>
          <div style="height: 500px; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.1);">
            <div ref="chartRef" style="width: 100%; height: 100%;"></div>
          </div>
        </el-col>

        <!-- 右侧：天气+日历 -->
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
  name: "StudentTeacherHome",
  components: {
    weather,
    Calender
  },
  data() {
    return {
      // 仅保留需要的核心数据（删除报修数/预约数）
      classroomTotal: "",     // 总教室数量
      currentAvailableNum: "",// 当日可用教室数
      activities: [],          // 教室公告
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
    this.loadAllData(); // 加载数据（仅请求需要的接口）
    this.getTodayAvailableBuildingData();
  },
  methods: {
    // 批量加载数据：删除报修数/预约数接口请求
    async loadAllData() {
      try {
        const [
          totalRes,
          availableRes,
          noticeRes
        ] = await Promise.all([
          request.get("/classroom/total"),                  // 总教室数量
          request.get(`/classroom/available/today?date=${this.today}`), // 当日可用教室数
          request.get("/notice/homePageNotice")             // 公告
        ]);

        // 总教室数量
        if (totalRes.code === "0") this.classroomTotal = totalRes.data;
        else if (totalRes.code === "-1") ElMessage.warning(totalRes.msg);
        else ElMessage.error("获取总教室数量失败：" + totalRes.msg);

        // 当日可用教室数
        if (availableRes.code === "0") this.currentAvailableNum = availableRes.data;
        else if (availableRes.code === "-1") ElMessage.warning(availableRes.msg);
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

    // 图表相关（逻辑完全不变）
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

    async getTodayAvailableBuildingData() {
      try {
        const res = await request.get(`/classroom/available/building?date=${this.today}`);
        if (res.code === "0" && Array.isArray(res.data) && res.data.length > 0) {
          this.chartData = res.data;
          this.updateChart();
        } else if (res.code === "-1") {
          ElMessage.warning(res.msg);
          this.useBackupData();
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
  }
};
</script>

<style scoped>
/* 样式不变，仅调整卡片宽度适配span12 */
.header-cards { margin-bottom: 20px; }
.card-item {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
  min-height: 180px; /* 统一高度 */
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.card-item:hover { transform: translateY(-5px); }
.card-tag { font-size: 12px; color: #67C23A; margin-bottom: 8px; }
.card-item:nth-child(2) .card-tag { color: #409EFF; }
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

:deep(.echarts-container) {
  width: 100%;
  height: 100%;
}
</style>