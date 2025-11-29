import request from "@/utils/request";
import { ElMessage } from "element-plus";

export default {
  name: "SelfInfo",
  data() {
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

    return {
      image: "",
      dialogVisible: false,
      identity: "",
      form: {
        username: "",
        name: "",
        phone: "", // 确保字段名是phone（与后端一致）
        status: 1,
        avatar: "",
        // 学生特有
        major: "",
        grade: "",
        // 老师特有
        department: "",
        title: ""
      },
      rules: {
        name: [
          { required: true, message: "请输入姓名", trigger: "blur" },
          { pattern: /^(?:[\u4E00-\u9FA5·]{2,10})$/, message: "必须由2到10个汉字组成", trigger: "blur" }
        ],
        phone: [ // 确保prop是phone（与表单绑定一致）
          { required: true, validator: checkPhone, trigger: "blur" }
        ],
        major: [{ required: true, message: "请输入专业", trigger: "blur" }],
        grade: [
          { required: true, message: "请输入年级", trigger: "blur" },
          { pattern: /^\d{4}级$/, message: "格式应为XXXX级（如2023级）", trigger: "blur" }
        ],
        department: [{ required: true, message: "请输入所属部门", trigger: "blur" }],
        title: [{ required: true, message: "请输入职称（如教授、讲师）", trigger: "blur" }]
      }
    };
  },
  created() {
    this.load();
    this.initAvatar();
  },
  methods: {
    // 关键修改：添加日志，验证后端返回的phone字段
    load() {
      const user = JSON.parse(sessionStorage.getItem("user"));
      // 1. 打印日志，确认sessionStorage中是否有user和phone字段
      console.log("从sessionStorage获取的用户信息：", user);
      console.log("用户信息中是否包含phone字段：", user ? "phone" in user : false);
      
      if (!user) {
        ElMessage.warning("未获取到用户信息，请重新登录");
        this.$router.push("/login");
        return;
      }

      this.identity = JSON.parse(sessionStorage.getItem("identity"));
      console.log("当前用户身份：", this.identity);

      // 2. 赋值时明确打印phone值，确认是否为空
      this.form.username = user.username || "";
      this.form.name = user.name || "";
      this.form.phone = user.phone || ""; // 赋值phone字段
      console.log("赋值给form.phone的值：", this.form.phone); // 查看是否拿到值
      
      this.form.avatar = user.avatar || "";
      this.form.status = user.status || 1;

      // 学生/老师特有字段赋值（不变）
      if (this.identity === "stu") {
        this.form.major = user.major || "";
        this.form.grade = user.grade || "";
      }
      if (this.identity === "teacher") {
        this.form.department = user.department || "";
        this.form.title = user.title || "";
      }
    },

    // 初始化头像（不变）
    initAvatar() {
      if (this.form.avatar) {
        request.get("/files/initAvatar/" + this.form.avatar).then((res) => {
          if (res.code === "0") {
            this.image = res.data.data;
          }
        });
      }
    },

    // 编辑、保存、取消等方法不变
    Edit() {
      this.dialogVisible = true;
    },

    save() {
      this.$refs.form.validate(async (valid) => {
        if (valid) {
          const submitData = { ...this.form };
          if (this.identity === "stu") {
            delete submitData.department;
            delete submitData.title;
          }
          if (this.identity === "teacher") {
            delete submitData.major;
            delete submitData.grade;
          }

          await request.put("/" + this.identity + "/update", submitData).then((res) => {
            if (res.code === "0") {
              ElMessage.success("修改成功");
              sessionStorage.setItem("user", JSON.stringify(submitData));
              this.dialogVisible = false;
            } else {
              ElMessage.error(res.msg || "修改失败");
            }
          });
        }
      });
    },

    cancel() {
      this.$refs.form.resetFields();
      this.dialogVisible = false;
    },

    EditPass() {
      this.$router.push("/changePass");
    }
  }
};