package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.ClassroomReservation;
import com.example.springboot.service.ClassroomReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/classroom/reservation")
public class ClassroomReservationController {

    @Autowired
    private ClassroomReservationService reservationService;

    // 提交预约
    @PostMapping("/submit")
    public Result<?> submitReservation(@RequestBody ClassroomReservation reservation) {
        // 增加参数非空校验
        if (reservation.getUsername() == null || reservation.getUsername().trim().isEmpty()) {
            return Result.error("-1", "用户名不能为空");
        }
        if (reservation.getClassroomId() == null || reservation.getClassroomId().trim().isEmpty()) {
            return Result.error("-1", "教室号不能为空");
        }
        if (reservation.getReserveDate() == null) {
            return Result.error("-1", "预约日期不能为空");
        }
        return reservationService.submitReservation(reservation);
    }

    // 查询用户预约记录
    @GetMapping("/myList")
    public Result<?> getMyReservations(@RequestParam String username) {
        if (username == null || username.trim().isEmpty()) {
            return Result.error("-1", "用户名不能为空");
        }
        return reservationService.getUserReservations(username);
    }

    // 取消预约
    @DeleteMapping("/cancel/{id}")
    public Result<?> cancelReservation(
            @PathVariable Integer id,
            @RequestParam String username) {
        if (id == null || username == null || username.trim().isEmpty()) {
            return Result.error("-1", "参数不能为空");
        }
        return reservationService.cancelReservation(id, username);
    }

    // 查询近7天预约次数
    @GetMapping("/countRecent")
    public Result<?> countRecentReservations(@RequestParam String username) {
        if (username == null || username.trim().isEmpty()) {
            return Result.error("-1", "用户名不能为空");
        }
        return reservationService.countRecentReservations(username);
    }

    /**
     * 管理员查询待审批预约列表（带分页）
     */
    @GetMapping("/pending")
    public Result<?> getPendingReservations(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        // 校验分页参数
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 10;
        return reservationService.getPendingReservations(pageNum, pageSize);
    }

    // 管理员审核预约
    @PostMapping("/audit")
    public Result<?> auditReservation(@RequestBody Map<String, Object> params) {
        try {
            if (params.get("id") == null || params.get("status") == null) {
                return Result.error("-1", "缺少必要参数");
            }
            Integer id = Integer.parseInt(params.get("id").toString());
            Integer status = Integer.parseInt(params.get("status").toString());
            String remark = params.get("adminRemark") != null ? params.get("adminRemark").toString() : null;
            return reservationService.auditReservation(id, status, remark);
        } catch (NumberFormatException e) {
            return Result.error("-1", "参数格式错误");
        }
    }
}