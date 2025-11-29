package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.ClassroomReservation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface ClassroomReservationMapper extends BaseMapper<ClassroomReservation> {
    // 保留原有方法，无需新增XML相关方法
    List<ClassroomReservation> selectByDateAndClassroom(
            @Param("reserveDate") Date reserveDate,
            @Param("classroomId") String classroomId,
            @Param("buildingId") String buildingId
    );

    List<ClassroomReservation> selectByUsername(@Param("username") String username);
}