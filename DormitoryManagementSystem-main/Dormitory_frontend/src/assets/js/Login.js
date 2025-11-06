import request from "@/utils/request";
const { ElMessage } = require("element-plus");

export default {
    name: "Login",
    data() {
        return {
            form: {
                username: "", // 统一存储账号（学生学号/老师工号/管理员账号）
                password: "",
                identity: "", // 身份：stu/teacher/dormManager/admin
            },
            rules: {
                username: [
                    { required: true, message: "请输入账号", trigger: "blur" },
                ],
                password: [{ required: true, message: "请输入密码", trigger: "blur" }],
                identity: [{ required: true, message: "请选择身份", trigger: "blur" }],
            },
        };
    },
    computed: {
        // 按钮禁用逻辑：所有字段填写后才可用（兼容老师身份）
        disabled() {
            const { username, password, identity } = this.form;
            return Boolean(username && password && identity);
        },
    },
    methods: {
        // 根据身份动态显示用户名占位符
        getUsernamePlaceholder() {
            switch (this.form.identity) {
                case "stu": return "请输入学号";
                case "teacher": return "请输入工号";
                case "dormManager": return "请输入管理员账号";
                case "admin": return "请输入后勤账号";
                default: return "请输入账号";
            }
        },
        login() {
            this.$refs.form.validate((valid) => {
                if (valid) {
                    // 拼接登录接口：根据身份动态生成（teacher对应/teacher/login）
                    const loginUrl = `/${this.form.identity}/login`;
                    
                    // 发送登录请求（老师的username已适配后端主键，直接传递form）
                    request.post(loginUrl, this.form).then((res) => {
                        if (res.code === "0") {
                            ElMessage({
                                message: "登录成功",
                                type: "success",
                            });
                            // 存储用户信息和身份（供其他页面使用）
                            window.sessionStorage.setItem("user", JSON.stringify(res.data));
                            window.sessionStorage.setItem("identity", JSON.stringify(this.form.identity));
                            // 跳转首页
                            this.$router.replace({ path: "/home" });
                        } else {
                            ElMessage({
                                message: res.msg || "登录失败，请检查账号密码",
                                type: "error",
                            });
                        }
                    }).catch((error) => {
                        // 捕获网络错误
                        ElMessage({
                            message: "网络错误，请稍后重试",
                            type: "error",
                        });
                        console.error("登录请求失败：", error);
                    });
                }
            });
        },
    },
};