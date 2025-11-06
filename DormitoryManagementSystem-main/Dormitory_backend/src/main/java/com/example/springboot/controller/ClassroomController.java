package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Classroom;
import com.example.springboot.service.ClassroomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * 教室Controller（接口入口）
 */
@RestController
@RequestMapping("/classroom")
public class ClassroomController {

    @Autowired
    private ClassroomService classroomService;

    /**
     * 教室查询接口（查询7天内的教室，无需传递queryDate）
     */
    @GetMapping("/list")
    public Result getClassroomList(
            @RequestParam(required = true) String identity, // 身份：stu/teacher
            @RequestParam(required = false) String buildingId) { // 教学楼ID（学生默认J1）

        try {
            // 学生身份强制查询J1教学楼
            if ("stu".equals(identity) || "student".equals(identity)) {
                buildingId = "J1";
            }

            // 调用Service查询7天内的教室（不再传递queryDate）
            List<Classroom> classrooms = classroomService.getClassroomsWithReserveStatus(buildingId);

            return Result.success(classrooms);

        } catch (Exception e) {
            return Result.error("-1", "查询教室失败：" + e.getMessage());
        }
    }
    @GetMapping("/availableTimeSlots")
    public Result getAvailableTimeSlots(
            @RequestParam String classroomId,
            @RequestParam String date) {
        try {
            // 验证参数
            if (classroomId == null || classroomId.trim().isEmpty() ||
                    date == null || date.trim().isEmpty()) {
                return Result.error("-1", "教室ID和日期不能为空");
            }
            // 验证日期格式
            LocalDate.parse(date);
            List<String> timeSlots = classroomService.getAvailableTimeSlots(classroomId, date);
            return Result.success(timeSlots);
        } catch (DateTimeParseException e) {
            return Result.error("-1", "日期格式错误，正确格式为yyyy-MM-dd");
        } catch (Exception e) {
            return Result.error("-1", "查询可用时间段失败：" + e.getMessage());
        }
    }

}