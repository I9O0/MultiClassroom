<template>
  <div>
    <el-button @click="dialogShow = true">打开测试弹窗</el-button>
    <el-dialog 
      title="极简测试弹窗" 
      v-model="dialogShow" 
      width="300px"
    >
      <p>弹窗正常显示！</p>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      dialogShow: false
    };
  }
};
</script>