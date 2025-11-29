package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.ClassroomRepair;
import java.util.Map;

public interface ClassroomRepairService extends IService<ClassroomRepair> {
    // 按状态统计报修数量（0=待处理，1=处理中，2=已解决）
    Map<String, Integer> countByStatus();

    // 按教学楼统计指定状态的报修数量
    Map<String, Integer> countPendingByBuilding(Integer status);
}