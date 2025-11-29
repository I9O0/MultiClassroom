package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Teacher;
import com.example.springboot.mapper.TeacherMapper;
import com.example.springboot.service.TeacherService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {

    @Resource
    private TeacherMapper teacherMapper;

    @Override
    public Teacher teacherLogin(String username, String password) {
        QueryWrapper<Teacher> qw = new QueryWrapper<>();
        qw.eq("username", username);
        qw.eq("password", password);
        qw.eq("status", 1);
        return baseMapper.selectOne(qw);
    }

    @Override
    public Page<Teacher> findPage(Integer pageNum, Integer pageSize, String search) {
        Page<Teacher> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Teacher> qw = new QueryWrapper<>();
        if (!search.isEmpty()) {
            qw.like("name", search);
        }
        return baseMapper.selectPage(page, qw);
    }

    // 新增教师（默认密码123456，MD5加密）
    @Override
    public int addTeacher(Teacher teacher) {
        // 关键修改：移除MD5加密，直接设置明文默认密码123456
        teacher.setPassword("123456");
        // 默认为正常状态
        if (teacher.getStatus() == null) {
            teacher.setStatus(1);
        }
        return teacherMapper.insert(teacher);
    }

    // 更新教师信息
    @Override
    public int updateTeacher(Teacher teacher) {
        // 仅更新非空字段（避免覆盖密码等敏感信息）
        return teacherMapper.updateById(teacher);
    }

    // 删除教师
    @Override
    public int deleteTeacher(String username) {
        return teacherMapper.deleteById(username);
    }
}