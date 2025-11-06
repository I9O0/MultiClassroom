import request from "@/utils/request";
const { ElMessage } = require("element-plus");

export default {
  name: "ClassroomManagerInfo", // 组件名修改
  components: {},
  data() {
    // 手机号验证（保持不变）
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

    // 密码二次验证（保持不变）
    const checkPass = (rule, value, callback) => {
      if (!this.editJudge) {
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
      showpassword: true,
      editJudge: true,
      judgeAddOrEdit: true,
      loading: true,
      disabled: false,
      judge: false,
      dialogVisible: false,
      search: "",
      currentPage: 1,
      pageSize: 10,
      total: 0,
      tableData: [],
      form: {
        username: "", // 工号（主键）
        name: "", // 姓名
        age: "", // 年龄
        gender: "", // 性别
        phoneNum: "", // 手机号（与ClassroomManager实体匹配）
        email: "", // 邮箱
        buildingId: "", // 关联楼宇（教室管理员可能管理特定楼宇）
        status: 1 // 状态（1-启用，0-禁用）
      },
      rules: {
        username: [
          { required: true, message: "请输入管理员账号", trigger: "blur" },
          {
            pattern: /^[a-zA-Z0-9]{4,9}$/,
            message: "必须由 4 到 9 个字母或数字组成",
            trigger: "blur",
          },
        ],
        name: [
          { required: true, message: "请输入姓名", trigger: "blur" },
          {
            pattern: /^(?:[\u4E00-\u9FA5·]{2,10})$/,
            message: "必须由 2 到 10 个汉字组成",
            trigger: "blur",
          },
        ],
        age: [
          { required: true, message: "请输入年龄", trigger: "blur" },
          { type: "number", message: "年龄必须为数字值", trigger: "blur" },
          {
            pattern: /^(1|[1-9]\d?|100)$/,
            message: "范围：1-100",
            trigger: "blur",
          },
        ],
        gender: [{ required: true, message: "请选择性别", trigger: "change" }],
        phoneNum: [{ required: true, validator: checkPhone, trigger: "blur" }],
        email: [
          { type: "email", message: "请输入正确的邮箱地址", trigger: "blur" },
        ],
        password: [
          { required: true, message: "请输入密码", trigger: "blur" },
          {
            min: 6,
            max: 32,
            message: "长度在 6 到 16 个字符",
            trigger: "blur",
          },
        ],
        checkPass: [{ validator: checkPass, trigger: "blur" }]
      },
      editDisplay: { display: "block" },
      display: { display: "none" },
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
    // 核心修改：加载教室管理员列表（接口路径修改）
    async load() {
      request.get("/classroomManager/find", { // 原/dormManager/find改为/classroomManager/find
        params: {
          pageNum: this.currentPage,
          pageSize: this.pageSize,
          search: this.search,
        },
      }).then((res) => {
        console.log(res);
        this.tableData = res.data.records;
        this.total = res.data.total;
        this.loading = false;
      }).catch((err) => {
        console.error("加载教室管理员列表失败：", err);
        ElMessage.error("加载失败，请重试");
        this.loading = false;
      });
    },

    // 重置搜索（接口路径修改）
    reset() {
      this.search = '';
      request.get("/classroomManager/find", { // 接口路径修改
        params: {
          pageNum: 1,
          pageSize: this.pageSize,
          search: this.search,
        },
      }).then((res) => {
        this.tableData = res.data.records;
        this.total = res.data.total;
        this.loading = false;
      });
    },

    filterTag(value, row) {
      return row.gender === value;
    },

    // 新增按钮逻辑（保持不变）
    add() {
      this.dialogVisible = true;
      this.$nextTick(() => {
        this.$refs.form.resetFields();
        this.judgeAddOrEdit = false;
        this.editDisplay = { display: "none" };
        this.disabled = false;
        this.form = {};
        this.judge = false;
      });
    },

    // 保存逻辑（新增/修改接口路径修改）
    save() {
      this.$refs.form.validate(async (valid) => {
        if (valid) {
          if (this.judge === false) {
            // 新增教室管理员
            request.post("/classroomManager/add", this.form).then((res) => { // 接口路径修改
              if (res.code === "0") {
                ElMessage.success("新增成功");
                this.search = "";
                this.load();
                this.dialogVisible = false;
              } else {
                ElMessage.error(res.msg || "新增失败");
              }
            });
          } else {
            // 修改教室管理员
            request.put("/classroomManager/update", this.form).then((res) => { // 接口路径修改
              if (res.code === "0") {
                ElMessage.success("修改成功");
                this.search = "";
                this.load();
                this.dialogVisible = false;
              } else {
                ElMessage.error(res.msg || "修改失败");
              }
            });
          }
        }
      });
    },

    cancel() {
      this.$refs.form.resetFields();
      this.display = { display: "none" };
      this.editJudge = true;
      this.disabled = true;
      this.showpassword = true;
      this.dialogVisible = false;
    },

    EditPass() {
      if (this.editJudge) {
        this.showpassword = false;
        this.display = { display: "flex" };
        this.disabled = false;
        this.editJudge = false;
      } else {
        this.showpassword = true;
        this.display = { display: "none" };
        this.editJudge = true;
        this.disabled = true;
      }
    },

    // 编辑逻辑（保持不变）
    handleEdit(row) {
      this.judge = true;
      this.dialogVisible = true;
      this.$nextTick(() => {
        this.$refs.form.resetFields();
        this.form = JSON.parse(JSON.stringify(row));
        this.judgeAddOrEdit = true;
        this.editDisplay = { display: "block" };
        this.disabled = true;
      });
    },

    // 删除逻辑（接口路径修改）
    async handleDelete(username) {
      request.delete(`/classroomManager/delete/${username}`).then((res) => { // 接口路径修改
        if (res.code === "0") {
          ElMessage.success("删除成功");
          this.search = "";
          this.load();
        } else {
          ElMessage.error(res.msg || "删除失败");
        }
      });
    },

    // 分页逻辑（保持不变）
    handleSizeChange(pageSize) {
      this.pageSize = pageSize;
      this.load();
    },
    handleCurrentChange(pageNum) {
      this.currentPage = pageNum;
      this.load();
    },
  },
};