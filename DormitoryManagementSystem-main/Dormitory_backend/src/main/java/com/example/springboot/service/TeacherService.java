package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.Teacher;

public interface TeacherService extends IService<Teacher> {
    // 已有的登录方法
    Teacher teacherLogin(String username, String password);

    // 分页查询方法
    Page<Teacher> findPage(Integer pageNum, Integer pageSize, String search);

    // 新增教师
    int addTeacher(Teacher teacher);

    // 更新教师信息（全量更新）
    int updateTeacher(Teacher teacher);

    // 删除教师
    int deleteTeacher(String username);
}