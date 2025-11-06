package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Student;
import com.example.springboot.mapper.StudentMapper;
import com.example.springboot.service.StudentService;
import org.springframework.stereotype.Service;

@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {

    @Override
    public Student stuLogin(String username, String password) {
        QueryWrapper<Student> qw = new QueryWrapper<>();
        qw.eq("username", username); // 对应实体类 username（主键）
        qw.eq("password", password); // 对应数据库 password 字段
        return baseMapper.selectOne(qw);
    }

    @Override
    public int addNewStudent(Student student) {
        // 新增时自动映射 username、password、name、major、grade、phone（对应phoneNum）、avatar
        return baseMapper.insert(student);
    }

    @Override
    public Page find(Integer pageNum, Integer pageSize, String search) {
        Page<Student> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Student> qw = new QueryWrapper<>();
        // 支持按姓名、学号、专业搜索（适配新字段）
        qw.like("name", search)
                .or().like("username", search)
                .or().like("major", search);
        return baseMapper.selectPage(page, qw);
    }

    @Override
    public int updateNewStudent(Student student) {
        // 更新时根据 username（主键）更新其他字段（含新字段 major、grade）
        return baseMapper.updateById(student);
    }

    @Override
    public int deleteStudent(String username) {
        // 按 username（主键）删除
        return baseMapper.deleteById(username);
    }

    @Override
    public int stuNum() {
        // 修复Long转int可能出现的溢出问题
        long count = baseMapper.selectCount(null);
        return count > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) count;
    }

    @Override
    public Student stuInfo(String username) {
        // 按 username 查询学生信息（含新字段）
        return baseMapper.selectById(username);
    }
}