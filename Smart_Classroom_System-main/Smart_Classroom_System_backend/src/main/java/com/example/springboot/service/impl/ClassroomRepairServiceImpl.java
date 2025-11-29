package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.ClassroomRepair;
import com.example.springboot.mapper.ClassroomRepairMapper;
import com.example.springboot.service.ClassroomRepairService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ClassroomRepairServiceImpl extends ServiceImpl<ClassroomRepairMapper, ClassroomRepair> implements ClassroomRepairService {

    @Resource
    private ClassroomRepairMapper classroomRepairMapper;

    /**
     * 按状态统计报修数量
     */
    @Override
    public Map<String, Integer> countByStatus() {
        Map<String, Integer> countMap = new HashMap<>();

        // 待处理数量 - 使用整数0而非字符串"pending"
        QueryWrapper<ClassroomRepair> pendingQw = new QueryWrapper<>();
        pendingQw.eq("status", 0);
        countMap.put("pending", Math.toIntExact(classroomRepairMapper.selectCount(pendingQw)));

        // 维修中数量 - 使用整数1而非字符串"repairing"
        QueryWrapper<ClassroomRepair> repairingQw = new QueryWrapper<>();
        repairingQw.eq("status", 1);
        countMap.put("repairing", Math.toIntExact(classroomRepairMapper.selectCount(repairingQw)));

        // 已解决数量 - 使用整数2而非字符串"solved"
        QueryWrapper<ClassroomRepair> solvedQw = new QueryWrapper<>();
        solvedQw.eq("status", 2);
        countMap.put("solved", Math.toIntExact(classroomRepairMapper.selectCount(solvedQw)));

        // 总数量
        countMap.put("total", countMap.get("pending") + countMap.get("repairing") + countMap.get("solved"));
        return countMap;
    }

    /**
     * 按教学楼统计待处理报修数量
     */
    @Override
    public Map<String, Integer> countPendingByBuilding(Integer status) {
        // 构建查询条件：按状态和教学楼分组统计
        QueryWrapper<ClassroomRepair> query = new QueryWrapper<>();
        query.eq("status", status)  // 按传入的状态筛选（如0=未处理）
                .groupBy("building_id")  // 按教学楼分组
                .select("building_id, count(*) as count");  // 统计每个教学楼的数量

        // 执行查询，返回Map列表（包含building_id和count字段）
        List<Map<String, Object>> list = baseMapper.selectMaps(query);

        // 转换为key=buildingId，value=count的Map
        Map<String, Integer> resultMap = new HashMap<>();
        for (Map<String, Object> map : list) {
            String buildingId = (String) map.get("building_id");
            Integer count = Integer.parseInt(map.get("count").toString());
            resultMap.put(buildingId, count);
        }
        return resultMap;
    }
}