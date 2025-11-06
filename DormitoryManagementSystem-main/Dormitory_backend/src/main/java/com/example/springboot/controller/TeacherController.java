package com.example.springboot.controller;

// 移除 MD5 加密相关的导入
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Teacher;
import com.example.springboot.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    // 1. 教师登录（删除 MD5 加密：直接用明文密码验证）
    @PostMapping("/login")
    public Result login(@RequestBody Teacher teacher, HttpSession session) {
        String username = teacher.getUsername();
        String password = teacher.getPassword(); // 前端传的明文密码，直接使用

        // 调用 service 时传明文密码（需确保 service 层也未做加密处理）
        Teacher loginTeacher = teacherService.teacherLogin(username, password);
        if (loginTeacher != null) {
            session.setAttribute("User", loginTeacher);
            session.setAttribute("Identity", "teacher");
            return Result.success(loginTeacher);
        } else {
            return Result.error("-1", "工号或密码错误，或账号已禁用");
        }
    }

    // 2. 新增教师（无加密逻辑，密码处理依赖 service 层默认值）
    @PostMapping("/add")
    public Result add(@RequestBody Teacher teacher) {
        // 校验工号是否已存在
        Teacher exist = teacherService.getById(teacher.getUsername());
        if (exist != null) {
            return Result.error("-1", "工号已存在");
        }
        int count = teacherService.addTeacher(teacher);
        return count > 0 ? Result.success("新增成功") : Result.error("-1", "新增失败");
    }

    // 3. 更新教师信息（无加密逻辑，禁止直接更新密码）
    @PutMapping("/update")
    public Result update(@RequestBody Teacher teacher) {
        // 禁止直接更新密码（密码通过专门接口修改）
        teacher.setPassword(null);
        int count = teacherService.updateTeacher(teacher);
        return count > 0 ? Result.success("更新成功") : Result.error("-1", "更新失败");
    }

    // 4. 删除教师（无加密相关逻辑）
    @DeleteMapping("/delete/{username}")
    public Result delete(@PathVariable String username) {
        int count = teacherService.deleteTeacher(username);
        return count > 0 ? Result.success("删除成功") : Result.error("-1", "删除失败");
    }

    // 5. 分页查询教师（无加密相关逻辑）
    @GetMapping("/find")
    public Result findPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "") String search) {
        Page<Teacher> page = teacherService.findPage(pageNum, pageSize, search);
        return Result.success(page);
    }

    // 6. 修改密码（删除 MD5 加密：明文比较旧密码，明文存储新密码）
    // 6. 修改密码（正确接收 oldPass 和 newPass，明文比对+明文存储）
    @PostMapping("/updatePass")
    public Result updatePass(@RequestBody Teacher teacher) {
        String username = teacher.getUsername();
        // 关键修改1：用 teacher.getOldPass() 接收前端的原密码（不再用 getPassword()）
        String oldPass = teacher.getOldPass();
        // 关键修改2：用 teacher.getNewPass() 接收前端的新密码（不再用 getPhone()）
        String newPass = teacher.getNewPass();

        // 1. 校验参数：避免空值导致的异常
        if (username == null || oldPass == null || newPass == null) {
            return Result.error("-1", "参数不能为空");
        }

        // 2. 查询当前教师信息（从数据库拿真实密码）
        Teacher current = teacherService.getById(username);
        if (current == null) {
            return Result.error("-1", "教师不存在");
        }

        // 3. 明文验证原密码（现在参数正确，能和数据库密码比对）
        System.out.println("数据库存储的密码：" + current.getPassword()); // 调试用，可删除
        System.out.println("前端传递的原密码：" + oldPass); // 调试用，可删除
        if (!current.getPassword().equals(oldPass)) {
            return Result.error("-1", "原密码错误");
        }

        // 4. 明文存储新密码
        current.setPassword(newPass);
        boolean update = teacherService.updateById(current);
        if (update) {
            return Result.success("密码修改成功");
        } else {
            return Result.error("-1", "密码修改失败");
        }
    }

    // 7. 获取教师详情（无加密相关逻辑）
    @GetMapping("/getInfo/{username}")
    public Result getInfo(@PathVariable String username) {
        Teacher teacher = teacherService.getById(username);
        return teacher != null ? Result.success(teacher) : Result.error("-1", "获取信息失败");
    }
}