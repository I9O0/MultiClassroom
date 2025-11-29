import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import './assets/css/global.css'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ELIcons from '@element-plus/icons-vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import axios from 'axios'
import VueAxios from 'vue-axios'

const app = createApp(App)
  .use(ElementPlus, { locale: zhCn })
  .use(VueAxios, axios)
  .use(router)
  .use(store)

// 注册所有图标
for (let iconName in ELIcons) {
  app.component(iconName, ELIcons[iconName])
}

// 配置axios默认路径
app.config.globalProperties.$axios = axios
axios.defaults.baseURL = 'http://localhost:8080'

app.mount('#app')