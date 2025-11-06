package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.Classroom;
import java.util.List;

public interface ClassroomService extends IService<Classroom> {
    // 保留原有方法（若其他地方使用）
    List<Classroom> listByBuildingAndDate(String building, String queryDate);
    List<String> getAvailableTimeSlots(String classroomId, String date);
    // 修改：移除date参数，查询7天内的教室
    List<Classroom> getClassroomsWithReserveStatus(String building);
}