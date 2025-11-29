import request from "@/utils/request";
import { ElMessage } from "element-plus";
import { Eye, EyeOff } from "@element-plus/icons-vue";

export default {
  name: "StuInfo",
  components: { Eye, EyeOff },
  data() {
    // 手机号验证
    const checkPhone = (rule, value, callback) => {
      const phoneReg = /^1[3|4|5|6|7|8][0-9]{9}$/;
      if (!value) {
        return callback(new Error("电话号码不能为空"));
      }
      setTimeout(() => {
        if (phoneReg.test(value)) {
          callback();
        } else {
          callback(new Error("电话号码格式不正确"));
        }
      }, 100);
    };

    // 密码一致性验证
    const checkPass = (rule, value, callback) => {
      if (value !== this.form.password) {
        callback(new Error("两次输入密码不一致!"));
      } else {
        callback();
      }
    };

    return {
      showpassword: true,
      judgeAddOrEdit: true, // true:新增 false:编辑
      loading: true,
      dialogVisible: false,
      search: "",
      currentPage: 1,
      pageSize: 10,
      total: 0,
      tableData: [],
      form: {
        username: "",
        name: "",
        major: "",
        grade: "",
        phoneNum: "",
        password: "123456", // 默认密码
        checkPass: "123456"
      },
      rules: {
        username: [
          { required: true, message: "请输入学号", trigger: "blur" },
          {
            pattern: /^[a-zA-Z0-9]{4,9}$/,
            message: "必须由4到9个字母或数字组成",
            trigger: "blur"
          }
        ],
        name: [
          { required: true, message: "请输入姓名", trigger: "blur" },
          {
            pattern: /^(?:[\u4E00-\u9FA5·]{2,10})$/,
            message: "必须由2到10个汉字组成",
            trigger: "blur"
          }
        ],
        major: [
          { required: true, message: "请输入专业", trigger: "blur" }
        ],
        grade: [
          { required: true, message: "请输入年级", trigger: "blur" },
          {
            pattern: /^\d{4}级$/,
            message: "格式应为XXXX级（如2023级）",
            trigger: "blur"
          }
        ],
        phoneNum: [
          { required: true, validator: checkPhone, trigger: "blur" }
        ],
        password: [
          { required: true, message: "请输入密码", trigger: "blur" },
          {
            min: 6,
            max: 32,
            message: "长度在6到16个字符",
            trigger: "blur"
          }
        ],
        checkPass: [
          { required: true, message: "请再次输入密码", trigger: "blur" },
          { validator: checkPass, trigger: "blur" }
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
    // 加载数据
    async load() {
      request.get("/stu/find", {
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

    // 新增
    add() {
      this.judgeAddOrEdit = true;
      this.dialogVisible = true;
      this.$nextTick(() => {
        this.$refs.form.resetFields();
        this.form.password = "123456";
        this.form.checkPass = "123456";
      });
    },

    // 编辑
    handleEdit(row) {
      this.judgeAddOrEdit = false;
      this.dialogVisible = true;
      this.$nextTick(() => {
        this.form = JSON.parse(JSON.stringify(row));
        this.form.checkPass = this.form.password;
      });
    },

    // 删除
    handleDelete(username) {
      this.$confirm("确定删除该学生吗？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        request.delete("/stu/delete/" + username).then((res) => {
          if (res.code === "0") {
            ElMessage.success("删除成功");
            this.load();
          } else {
            ElMessage.error(res.msg);
          }
        });
      });
    },

    // 保存
    save() {
      this.$refs.form.validate(async (valid) => {
        if (valid) {
          if (this.judgeAddOrEdit) {
            // 新增
            await request.post("/stu/add", this.form).then((res) => {
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
            await request.put("/stu/update", this.form).then((res) => {
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

    // 分页相关
    handleSizeChange(pageSize) {
      this.pageSize = pageSize;
      this.load();
    },
    handleCurrentChange(pageNum) {
      this.currentPage = pageNum;
      this.load();
    },
    handleSelectionChange(val) {
      this.multipleSelection = val;
    }
  }
};