package com.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Student;
import com.example.springboot.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/stu") // 对应老师端的 "/teacher"，学生接口统一前缀
public class StudentController {

    @Autowired
    private StudentService studentService; // 替换为学生Service

    // 1. 学生登录（复刻老师端登录：明文密码、session存储、错误提示）
    @PostMapping("/login")
    public Result login(@RequestBody Student student, HttpSession session) {
        String username = student.getUsername(); // 学生：学号（对应老师端“工号”）
        String password = student.getPassword(); // 前端传明文密码（与老师端一致）

        // 调用学生Service的登录方法（对应老师端 teacherLogin）
        Student loginStudent = studentService.stuLogin(username, password);
        if (loginStudent != null) {
            session.setAttribute("User", loginStudent);
            session.setAttribute("Identity", "stu"); // 学生身份标识（对应老师端“teacher”）
            return Result.success(loginStudent);
        } else {
            // 错误提示替换为“学号”相关（复刻老师端提示格式）
            return Result.error("-1", "学号或密码错误，或账号已禁用");
        }
    }

    // 2. 新增学生（复刻老师端新增：校验唯一性、调用学生Service）
    @PostMapping("/add")
    public Result add(@RequestBody Student student) {
        // 校验学号是否已存在（对应老师端“工号”校验）
        Student exist = studentService.getById(student.getUsername());
        if (exist != null) {
            return Result.error("-1", "学号已存在");
        }
        // 调用学生Service的新增方法（对应老师端 addTeacher）
        int count = studentService.addNewStudent(student);
        return count > 0 ? Result.success("新增成功") : Result.error("-1", "新增失败");
    }

    // 3. 更新学生信息（复刻老师端：禁止更新密码、调用学生Service）
    @PutMapping("/update")
    public Result update(@RequestBody Student student) {
        // 核心：禁止直接更新密码（与老师端逻辑完全一致，密码通过专门接口修改）
        student.setPassword(null);
        // 调用学生Service的更新方法（对应老师端 updateTeacher）
        int count = studentService.updateNewStudent(student);
        return count > 0 ? Result.success("更新成功") : Result.error("-1", "更新失败");
    }

    // 4. 删除学生（复刻老师端：路径参数、调用学生Service）
    @DeleteMapping("/delete/{username}")
    public Result delete(@PathVariable String username) {
        // 调用学生Service的删除方法（对应老师端 deleteTeacher）
        int count = studentService.deleteStudent(username);
        return count > 0 ? Result.success("删除成功") : Result.error("-1", "删除失败");
    }

    // 5. 分页查询学生（复刻老师端：参数、分页逻辑、调用学生Service）
    @GetMapping("/find")
    public Result findPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "") String search) {
        // 调用学生Service的分页查询方法（对应老师端 findPage，学生Service方法名为 find）
        Page<Student> page = studentService.find(pageNum, pageSize, search);
        return Result.success(page);
    }

    // 6. 修改学生密码（1:1复刻老师端：明文比对、明文存储）
    @PostMapping("/updatePass")
    public Result updatePass(@RequestBody Student student) {
        String username = student.getUsername(); // 学生学号（对应老师端“工号”）
        String oldPass = student.getOldPass();   // 接收前端原密码（与老师端一致）
        String newPass = student.getNewPass();   // 接收前端新密码（与老师端一致）

        // 1. 参数校验（完全复刻老师端：空值判断）
        if (username == null || oldPass == null || newPass == null) {
            return Result.error("-1", "参数不能为空");
        }

        // 2. 查询当前学生（对应老师端 getById，学生用 getById 或 stuInfo 均可，保持与Service一致）
        Student current = studentService.getById(username);
        if (current == null) {
            return Result.error("-1", "学生不存在"); // 提示替换为“学生”
        }

        // 3. 明文验证原密码（与老师端逻辑一致，无加密）
        System.out.println("数据库存储的密码：" + current.getPassword());
        System.out.println("前端传递的原密码：" + oldPass);
        if (!current.getPassword().equals(oldPass)) {
            return Result.error("-1", "原密码错误");
        }

        // 4. 明文存储新密码（与老师端一致）
        current.setPassword(newPass);
        boolean update = studentService.updateById(current);
        return update ? Result.success("密码修改成功") : Result.error("-1", "密码修改失败");
    }
    @GetMapping("/num")
    public Result<?> getStudentNum() {
        int num = studentService.stuNum(); // 用 Long 接收，解决类型问题
        return Result.success(num);
    }

    // 7. 获取学生详情（复刻老师端 getInfo：参数、返回逻辑）
    @GetMapping("/getInfo/{username}")
    public Result getInfo(@PathVariable String username) {
        // 调用学生Service的详情方法（对应老师端 getById，学生用 stuInfo 匹配Service方法名）
        Student student = studentService.stuInfo(username);
        return student != null ? Result.success(student) : Result.error("-1", "获取信息失败");
    }
}