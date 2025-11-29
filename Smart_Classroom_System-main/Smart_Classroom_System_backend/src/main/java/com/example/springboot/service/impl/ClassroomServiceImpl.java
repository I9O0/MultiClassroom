// Dormitory_backend/src/main/java/com/example/springboot/service/impl/ClassroomServiceImpl.java
package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Classroom;
import com.example.springboot.entity.ClassroomReservation;
import com.example.springboot.mapper.ClassroomMapper;
import com.example.springboot.mapper.ClassroomReservationMapper;
import com.example.springboot.service.ClassroomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ClassroomServiceImpl extends ServiceImpl<ClassroomMapper, Classroom> implements ClassroomService {
    @Autowired
    private ClassroomReservationMapper classroomReservationMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public List<Classroom> listByBuildingAndDate(String building, String queryDate) {
        QueryWrapper<Classroom> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("building_id", building)
                .eq("date", queryDate)
                .eq("status", 1); // 可用状态
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<Classroom> getClassroomsWithReserveStatus(String building) {
        QueryWrapper<Classroom> queryWrapper = new QueryWrapper<>();

        // 计算当前日期往后7天的范围
        LocalDate today = LocalDate.now();
        LocalDate next7Day = today.plusDays(6);

        String startDate = today.format(DATE_FORMATTER);
        String endDate = next7Day.format(DATE_FORMATTER);

        // 日期范围查询
        queryWrapper.between("date", startDate, endDate)
                .eq("status", 1);

        if (building != null && !building.isEmpty()) {
            queryWrapper.eq("building_id", building);
        }

        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<String> getAvailableTimeSlots(String classroomId, String date) {
        List<String> allTimeSlots = Arrays.asList(
                "08:00-10:00", "10:00-12:00",
                "14:00-16:00", "16:00-18:00", "18:00-20:00", "20:00-22:00"
        );

        QueryWrapper<ClassroomReservation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("classroom_id", classroomId)
                .eq("reserve_date", LocalDate.parse(date, DATE_FORMATTER)) // 适配LocalDate
                .in("status", 0, 1);

        List<ClassroomReservation> reservations = classroomReservationMapper.selectList(queryWrapper);

        Set<String> occupiedTimes = reservations.stream()
                .map(r -> r.getStartTime() + "-" + r.getEndTime())
                .collect(Collectors.toSet());

        return allTimeSlots.stream()
                .filter(slot -> !occupiedTimes.contains(slot))
                .collect(Collectors.toList());
    }
    @Override
    public long getCurrentAvailableCount() {
        // 1. 查询所有可用教室（status=1）
        QueryWrapper<Classroom> classroomQuery = new QueryWrapper<>();
        classroomQuery.eq("status", 1);
        List<Classroom> availableClassrooms = list(classroomQuery);
        if (availableClassrooms.isEmpty()) {
            return 0;
        }

        // 2. 提取教室ID列表
        List<String> classroomIds = availableClassrooms.stream()
                .map(Classroom::getClassroomId)
                .collect(Collectors.toList());

        // 3. 过滤当前时间有已通过预约的教室
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        String currentTimeStr = now.format(DateTimeFormatter.ofPattern("HH:mm"));

        QueryWrapper<ClassroomReservation> conflictQuery = new QueryWrapper<>();
        conflictQuery.in("classroom_id", classroomIds)
                .eq("reserve_date", today) // 今天的预约
                .eq("status", 1) // 已通过的预约
                .lt("start_time", currentTimeStr)
                .gt("end_time", currentTimeStr); // 正在进行的预约

        // 有冲突的教室ID
        List<String> conflictClassroomIds = classroomReservationMapper.selectObjs(conflictQuery)
                .stream()
                .map(obj -> (String) obj)
                .collect(Collectors.toList());

        // 4. 可用教室数 = 总可用数 - 冲突数
        return availableClassrooms.size() - conflictClassroomIds.size();
    }
}