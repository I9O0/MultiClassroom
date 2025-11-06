package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.entity.Classroom;
import com.example.springboot.entity.ClassroomReservation;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

// 继承 BaseMapper<Classroom>，获得 CRUD 基础方法
public interface ClassroomMapper extends BaseMapper<Classroom> {
    // 无需定义 selectByBuildingId 方法，删除该方法的声明
    // 自定义分页查询，关联教室表获取classroomName
    IPage<Map<String, Object>> selectPendingWithClassroom(Page<ClassroomReservation> page, @Param("ew") QueryWrapper<ClassroomReservation> query);
}