import axios from 'axios'

const request = axios.create({
    baseURL: '/api',  // 已包含/api前缀，接口调用时无需重复添加
    timeout: 5000
})

// 存储教学楼列表（全局缓存，仅加载一次）
let buildingList = [];

// 预加载教学楼列表（从正确接口/building/listAll获取）
const preloadBuildingList = async () => {
    if (buildingList.length === 0) {
        try {
            // 调用正确接口获取教学楼列表（自动带上/api前缀）
            const res = await request.get('/building/listAll');
            if (res.code === "0") {
                buildingList = res.data || [];  // 缓存{buildingId, buildingName}列表
            }
        } catch (err) {
            console.error('预加载教学楼列表失败', err);
        }
    }
};

// 初始化预加载（页面加载时就获取，确保后续拦截时已有数据）
preloadBuildingList();

let token = '';
// request 拦截器（修改为作用于request实例，原代码用了axios导致不生效）
request.interceptors.request.use(
    function (config) {
        // 1. 处理token（保留你的原有逻辑）
        let user = JSON.parse(window.sessionStorage.getItem('access-user') || '{}');
        if (user.token) {
            token = user.token;
        }
        config.headers.common['token'] = token;

        // 2. 拦截/building/getBuildingName接口，返回本地匹配结果
        if (config.url === '/building/getBuildingName') {
            const buildingId = config.params?.buildingId;  // 提取请求参数中的buildingId
            // 从缓存的buildingList中匹配名称
            const targetBuilding = buildingList.find(b => b.buildingId === buildingId);
            const buildingName = targetBuilding ? targetBuilding.buildingName : buildingId || '未知教学楼';
            
            // 直接返回结果，不发送真实请求（避免404）
            return new Promise(resolve => {
                resolve({
                    data: {
                        code: "0",
                        data: buildingName,
                        msg: "success"
                    }
                });
            });
        }

        return config;
    },
    function (error) {
        console.error("请求拦截器错误: ", error);
        return Promise.reject(error);
    }
);

// response 拦截器（保留原有逻辑）
request.interceptors.response.use(
    response => {
        let res = response.data;
        // 处理文件类型响应
        if (response.config.responseType === 'blob') {
            return res;
        }
        // 处理字符串类型响应
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res;
        }
        return res;
    },
    error => {
        console.log('响应错误: ' + error);
        return Promise.reject(error);
    }
);

export default request;