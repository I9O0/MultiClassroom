package com.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.User;
import com.example.springboot.entity.Manager;
import com.example.springboot.service.ManagerService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/manager")
public class managerController {

    @Resource
    private ManagerService managerService;

    /**
     * 管理员添加
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody Manager manager) {
        int i = managerService.addNewmanager(manager);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "添加失败");
        }
    }

    /**
     * 管理员信息更新
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody Manager manager) {
        int i = managerService.updateNewmanager(manager);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "更新失败");
        }
    }

    /**
     * 管理员删除
     */
    @DeleteMapping("/delete/{username}")
    public Result<?> delete(@PathVariable String username) {
        int i = managerService.deletemanager(username);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "删除失败");
        }
    }

    /**
     * 管理员查找
     */
    @GetMapping("/find")
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "") String search) {
        Page page = managerService.find(pageNum, pageSize, search);
        if (page != null) {
            return Result.success(page);
        } else {
            return Result.error("-1", "查询失败");
        }
    }

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<?> login(@RequestBody User user, HttpSession session) {
        Object o = managerService.managerLogin(user.getUsername(), user.getPassword());
        if (o != null) {
            // 假设o是manager类型，包含getUsername()和getName()方法
            Manager manager = (Manager) o;
            session.setAttribute("identity", "manager");
            session.setAttribute("User", o);
            session.setAttribute("username", manager.getUsername()); // 存储处理人ID
            session.setAttribute("name", manager.getName()); // 存储处理人姓名
            return Result.success(o);
        }
        return Result.error("-1", "用户名或密码错误");
    }
    /**
     * 管理员修改密码（明文验证+明文存储，对齐学生/教师逻辑）
     */
    @PostMapping("/updatePass")
    public Result<?> updatePass(@RequestBody Manager manager) {
        String username = manager.getUsername();
        String oldPass = manager.getOldPass();   // 接收前端原密码
        String newPass = manager.getNewPass();   // 接收前端新密码

        // 1. 参数校验
        if (username == null || oldPass == null || newPass == null) {
            return Result.error("-1", "参数不能为空");
        }

        // 2. 查询当前管理员信息
        Manager current = managerService.getById(username);
        if (current == null) {
            return Result.error("-1", "管理员不存在");
        }

        // 3. 明文验证原密码
        if (!current.getPassword().equals(oldPass)) {
            return Result.error("-1", "原密码错误");
        }

        // 4. 明文存储新密码
        current.setPassword(newPass);
        boolean update = managerService.updateById(current);
        return update ? Result.success("密码修改成功") : Result.error("-1", "密码修改失败");
    }
}
