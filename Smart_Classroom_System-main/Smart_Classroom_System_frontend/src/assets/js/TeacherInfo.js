import request from "@/utils/request";

const {ElMessage} = require("element-plus");

export default {
    name: "TeacherInfo",
    data() {
        // 手机号验证
        const checkPhone = (rule, value, callback) => {
            const phoneReg = /^1[3|4|5|6|7|8][0-9]{9}$/;
            if (!value) {
                return callback(new Error("电话号码不能为空"));
            }
            setTimeout(() => {
                if (!Number.isInteger(+value)) {
                    callback(new Error("请输入数字值"));
                } else {
                    if (phoneReg.test(value)) {
                        callback();
                    } else {
                        callback(new Error("电话号码格式不正确"));
                    }
                }
            }, 100);
        };
        
        // 密码验证
        const checkPass = (rule, value, callback) => {
            if (!this.judge) {
                if (value === "") {
                    callback(new Error("请再次输入密码"));
                } else if (value !== this.form.password) {
                    callback(new Error("两次输入密码不一致!"));
                } else {
                    callback();
                }
            } else {
                callback();
            }
        };
        
        return {
            loading: true,
            dialogVisible: false,
            detailDialog: false,
            judge: false, // false-新增，true-编辑
            search: "",
            currentPage: 1,
            pageSize: 10,
            total: 0,
            tableData: [],
            detail: {},
            form: {
                username: "",
                name: "",
                department: "",
                title: "",
                phone: "",
                status: 1,
                password: "",
                checkPass: ""
            },
            rules: {
                username: [
                    {required: true, message: "请输入工号", trigger: "blur"},
                    {
                        pattern: /^[a-zA-Z0-9]{4,10}$/,
                        message: "必须由4到10个字母或数字组成",
                        trigger: "blur"
                    }
                ],
                name: [
                    {required: true, message: "请输入姓名", trigger: "blur"},
                    {
                        pattern: /^(?:[\u4E00-\u9FA5·]{2,10})$/,
                        message: "必须由2到10个汉字组成",
                        trigger: "blur"
                    }
                ],
                department: [
                    {required: true, message: "请输入所属部门", trigger: "blur"}
                ],
                title: [
                    {required: true, message: "请输入职称", trigger: "blur"}
                ],
                phone: [
                    {required: true, validator: checkPhone, trigger: "blur"}
                ],
                status: [
                    {required: true, message: "请选择状态", trigger: "change"}
                ],
                password: [
                    {required: true, message: "请输入密码", trigger: "blur"},
                    {
                        min: 6,
                        max: 32,
                        message: "长度在6到16个字符",
                        trigger: "blur"
                    }
                ],
                checkPass: [
                    {validator: checkPass, trigger: "blur"}
                ]
            }
        };
    },
    created() {
        this.load();
        this.loading = true;
        setTimeout(() => {
            this.loading = false;
        }, 1000);
    },
    methods: {
        // 加载教师列表
        async load() {
            request.get("/teacher/find", {
                params: {
                    pageNum: this.currentPage,
                    pageSize: this.pageSize,
                    search: this.search
                }
            }).then((res) => {
                this.tableData = res.data.records;
                this.total = res.data.total;
                this.loading = false;
            });
        },
        
        // 重置搜索
        reset() {
            this.search = '';
            this.currentPage = 1;
            this.load();
        },
        
        // 新增教师
        add() {
            this.dialogVisible = true;
            this.$nextTick(() => {
                this.$refs.form.resetFields();
                this.judge = false;
                this.form = {status: 1};
            });
        },
        
        // 保存（新增/编辑）
        save() {
            this.$refs.form.validate((valid) => {
                if (valid) {
                    if (!this.judge) {
                        // 新增
                        request.post("/teacher/add", this.form).then((res) => {
                            if (res.code === "0") {
                                ElMessage.success("新增成功");
                                this.load();
                                this.dialogVisible = false;
                            } else {
                                ElMessage.error(res.msg);
                            }
                        });
                    } else {
                        // 编辑
                        request.put("/teacher/update", this.form).then((res) => {
                            if (res.code === "0") {
                                ElMessage.success("修改成功");
                                this.load();
                                this.dialogVisible = false;
                            } else {
                                ElMessage.error(res.msg);
                            }
                        });
                    }
                }
            });
        },
        
        // 取消
        cancel() {
            this.$refs.form.resetFields();
            this.dialogVisible = false;
        },
        
        // 编辑教师
        handleEdit(row) {
            this.judge = true;
            this.dialogVisible = true;
            this.$nextTick(() => {
                this.$refs.form.resetFields();
                this.form = JSON.parse(JSON.stringify(row));
            });
        },
        
        // 删除教师
        handleDelete(username) {
            this.$confirm("确定要删除该教师吗？", "提示", {
                confirmButtonText: "确定",
                cancelButtonText: "取消",
                type: "warning"
            }).then(() => {
                request.delete(`/teacher/delete/${username}`).then((res) => {
                    if (res.code === "0") {
                        ElMessage.success("删除成功");
                        this.load();
                    } else {
                        ElMessage.error(res.msg);
                    }
                });
            }).catch(() => {
                ElMessage.info("已取消删除");
            });
        },
        
        // 查看详情
        showDetail(row) {
            this.detailDialog = true;
            this.detail = JSON.parse(JSON.stringify(row));
        },
        
        // 改变每页条数
        handleSizeChange(pageSize) {
            this.pageSize = pageSize;
            this.load();
        },
        
        // 改变页码
        handleCurrentChange(pageNum) {
            this.currentPage = pageNum;
            this.load();
        }
    }
};