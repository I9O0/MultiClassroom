package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.common.UID;
import com.example.springboot.entity.Admin;
import com.example.springboot.entity.User;
import com.example.springboot.service.AdminService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/admin")
public class AdminController {

    String uid = new UID().produceUID();

    @Resource
    private AdminService adminService;

    /**
     * 超级管理员登录
     */
    @PostMapping("/login")
    public Result<?> login(@RequestBody User user, HttpSession session) {
        Object o = adminService.adminLogin(user.getUsername(), user.getPassword());
        if (o != null) {
            // 假设o是Admin类型，包含getUsername()和getName()方法
            Admin admin = (Admin) o;
            session.setAttribute("identity", "admin");
            session.setAttribute("User", o);
            session.setAttribute("username", admin.getUsername()); // 存储处理人ID
            session.setAttribute("name", admin.getName()); // 存储处理人姓名
            return Result.success(o);
        }
        return Result.error("-1", "用户名或密码错误");
    }

    /**
     * 超级管理员信息更新
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody Admin admin) {
        // 1. 查询原始数据
        Admin existingAdmin = adminService.getById(admin.getUsername());
        if (existingAdmin == null) {
            return Result.error("-1", "超级管理员不存在");
        }

        // 2. 覆盖非空字段（按需添加所有需要更新的字段）
        if (admin.getName() != null) existingAdmin.setName(admin.getName());
        if (admin.getGender() != null) existingAdmin.setGender(admin.getGender());
        if (admin.getAge() != 0) existingAdmin.setAge(admin.getAge()); // 假设age默认0，实际按业务调整
        if (admin.getPhoneNum() != null) existingAdmin.setPhoneNum(admin.getPhoneNum());
        if (admin.getEmail() != null) existingAdmin.setEmail(admin.getEmail());
        // 头像单独通过上传接口更新，此处可忽略

        // 3. 执行更新
        int i = adminService.updateAdmin(existingAdmin);
        return i == 1 ? Result.success() : Result.error("-1", "更新失败");
    }
    /**
     * 超级管理员修改密码（明文验证+明文存储）
     */
    @PostMapping("/updatePass")
    public Result<?> updatePass(@RequestBody Admin admin) {
        String username = admin.getUsername();
        String oldPass = admin.getOldPass();
        String newPass = admin.getNewPass();

        // 1. 参数校验
        if (username == null || oldPass == null || newPass == null) {
            return Result.error("-1", "参数不能为空");
        }

        // 2. 查询当前超级管理员
        Admin current = adminService.getById(username);
        if (current == null) {
            return Result.error("-1", "超级管理员不存在");
        }

        // 3. 验证原密码
        if (!current.getPassword().equals(oldPass)) {
            return Result.error("-1", "原密码错误");
        }

        // 4. 更新新密码
        current.setPassword(newPass);
        int update = adminService.updateAdmin(current);
        return update == 1 ? Result.success("密码修改成功") : Result.error("-1", "密码修改失败");
    }
}
