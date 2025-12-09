package com.example.springboot.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.ClassroomReservation;
import com.example.springboot.mapper.ClassroomReservationMapper;
import com.example.springboot.service.ClassroomReservationService;
import com.example.springboot.unit.ClassroomPermissionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/classroom/reservation")
public class ClassroomReservationController {

    @Autowired
    private ClassroomReservationService reservationService;
    @Resource
    private ClassroomReservationMapper reservationMapper;
    // 提交预约
    @PostMapping("/submit")
    public Result<?> submitReservation(@RequestBody ClassroomReservation reservation, HttpSession session) {
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
        String identity = (String) session.getAttribute("identity");
        if (identity == null) {
            return Result.error("-1", "请先登录");
        }
        reservation.setIdentity(identity); // 设置身份信息
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
    /**
     * 管理员查询待审批预约列表（带分页和身份筛选）
     */
    /**
     * 教室预约审批列表（支持状态筛选）
     * @param status 状态：0=待审核，1=已通过，2=已过期（null=查询全部）
     */
    @GetMapping("/pending")
    public Result<?> getReservationList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String identity,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String buildingId,  // 新增：教学楼筛选
            @RequestParam(required = false) String reserveDate, // 新增：预约日期筛选
            @RequestParam(required = false) String keyword) {    // 新增：搜索关键词

        Page<ClassroomReservation> page = new Page<>(pageNum, pageSize);
        // 传递所有筛选参数到Service
        Page<ClassroomReservation> resultPage = reservationService.queryReservationList(
                page, identity, status, buildingId, reserveDate, keyword);
        return Result.success(resultPage);
    }
    // 管理员审核预约
    @PostMapping("/audit")
    public Result<?> auditReservation(@RequestBody Map<String, Object> params) {
        try {
            // 增加审批意见必填校验
            if (params.get("id") == null || params.get("status") == null || params.get("adminRemark") == null) {
                return Result.error("-1", "缺少必要参数：ID、状态和审批意见不能为空");
            }
            // 校验意见不为空字符串
            String remark = params.get("adminRemark").toString().trim();
            if (remark.isEmpty()) {
                return Result.error("-1", "审批意见不能为空");
            }
            Integer id = Integer.parseInt(params.get("id").toString());
            Integer status = Integer.parseInt(params.get("status").toString());
            return reservationService.auditReservation(id, status, remark);
        } catch (NumberFormatException e) {
            return Result.error("-1", "参数格式错误");
        }
    }
    // 文件：Dormitory_backend/src/main/java/com/example/springboot/controller/ClassroomReservationController.java
    @GetMapping("/admin/classroomDateReservations")
    public Result<?> getClassroomDateReservations(
            @RequestParam String classroomId,
            @RequestParam String date,
            HttpSession session) {
        // 权限调整：允许管理员和宿管访问
        String identity = (String) session.getAttribute("identity");
        if (!ClassroomPermissionUtil.isAdminOrmanager(identity)) {
            return Result.error("-1", "无权限：仅管理员和宿管可查看");
        }

        // 原有参数校验和查询逻辑不变...
        if (classroomId == null || classroomId.trim().isEmpty() ||
                date == null || date.trim().isEmpty()) {
            return Result.error("-1", "教室ID和日期不能为空");
        }

        try {
            LocalDate reserveDate = LocalDate.parse(date);
            QueryWrapper<ClassroomReservation> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("classroom_id", classroomId)
                    .eq("reserve_date", reserveDate)
                    .orderByAsc("start_time");
            List<ClassroomReservation> reservations = reservationService.list(queryWrapper);
            return Result.success(reservations);
        } catch (DateTimeParseException e) {
            return Result.error("-1", "日期格式错误，正确格式为yyyy-MM-dd");
        } catch (Exception e) {
            return Result.error("-1", "查询失败：" + e.getMessage());
        }
    }
    @GetMapping("/manager/classroomDateReservations")
    public Result<?> getmanagerClassroomReservations(
            @RequestParam String classroomId,
            @RequestParam String buildingId,
            @RequestParam String date,
            HttpSession session) {
        // 权限校验：仅宿管可访问
        String identity = (String) session.getAttribute("identity");
        if (!"manager".equals(identity)) {
            return Result.error("-1", "无权限：仅宿管可查看");
        }

        // 参数校验
        if (classroomId == null || classroomId.trim().isEmpty() ||
                buildingId == null || buildingId.trim().isEmpty() ||
                date == null || date.trim().isEmpty()) {
            return Result.error("-1", "教室ID、教学楼ID和日期不能为空");
        }

        try {
            // 解析日期
            LocalDate reserveDate = LocalDate.parse(date);
            // 多条件查询（确保精准匹配）
            QueryWrapper<ClassroomReservation> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("classroom_id", classroomId)
                    .eq("building_id", buildingId) // 关键：匹配教学楼
                    .eq("reserve_date", reserveDate)
                    .orderByAsc("start_time");

            List<ClassroomReservation> reservations = reservationService.list(queryWrapper);
            return Result.success(reservations);
        } catch (DateTimeParseException e) {
            return Result.error("-1", "日期格式错误，正确格式为yyyy-MM-dd");
        } catch (Exception e) {
            return Result.error("-1", "查询预约详情失败：" + e.getMessage());
        }
    }
    // 在getAuditedReservations接口中增强筛选逻辑
    @GetMapping("/audited")
    public Result<?> getAuditedReservations(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer checkStatus,
            @RequestParam(required = false) String buildingId,  // 教学楼筛选
            @RequestParam(required = false) String reserveDate, // 日期筛选
            @RequestParam(required = false) String keyword) {    // 搜索关键词（如教室号、姓名）

        Page<ClassroomReservation> page = new Page<>(pageNum, pageSize);
        Page<ClassroomReservation> resultPage = reservationService.getAuditedReservations(
                page, username, checkStatus, buildingId, reserveDate, keyword);
        return Result.success(resultPage);
    }

    /**
     * 签到接口
     */
    @PostMapping("/checkIn/{id}")
    public Result<?> checkIn(@PathVariable Integer id, @RequestParam String username) {
        if (id == null || username == null || username.trim().isEmpty()) {
            return Result.error("-1", "参数不能为空");
        }
        return reservationService.checkIn(id, username);
    }

    /**
     * 签退接口
     */
    @PostMapping("/checkOut/{id}")
    public Result<?> checkOut(@PathVariable Integer id, @RequestParam String username) {
        if (id == null || username == null || username.trim().isEmpty()) {
            return Result.error("-1", "参数不能为空");
        }
        return reservationService.checkOut(id, username);
    }
    // 文件：Dormitory_backend/src/main/java/com/example/springboot/controller/ClassroomReservationController.java
    /**
     * 管理员和宿管查询签到退记录
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param username 可选：用户名（筛选特定用户）
     * @param identity 可选：身份（stu/teacher，筛选特定身份）
     * @param buildingId 可选：教学楼ID（筛选特定楼宇）
     * @param session 用于权限校验
     */
    // 文件：ClassroomReservationController.java
    @GetMapping("/admin/checkRecords")
    public Result<?> getCheckRecords(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String identity,
            @RequestParam(required = false) String buildingId,
            @RequestParam(required = false) String date,
            HttpSession session) {

        // 权限校验
        String currentIdentity = (String) session.getAttribute("identity");
        if (!ClassroomPermissionUtil.isAdminOrmanager(currentIdentity)) {
            return Result.error("-1", "无权限：仅管理员和宿管可查看");
        }

        Page<ClassroomReservation> page = new Page<>(pageNum, pageSize);
        Page<ClassroomReservation> resultPage = reservationService.getCheckRecordsByBuilding(
                page, username, identity, buildingId, date);
        return Result.success(resultPage);
    }
    /**
     * 统计今日预约总次数
     */
    @GetMapping("/todayCount")
    public Result<?> getTodayReservationCount() {
        // 今日00:00:00至23:59:59
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);

        QueryWrapper<ClassroomReservation> query = new QueryWrapper<>();
        query.between("create_time", todayStart, todayEnd); // 按提交时间筛选今日

        long count = reservationService.count(query);
        return Result.success(count);
    }
    /**
     * 查询用户未来3天的预约记录
     */
    @GetMapping("/myRecent")
    public Result<?> getMyRecentReservations(
            @RequestParam String username,
            HttpSession session) {
        // 权限校验：仅本人可查询
        String loginUser = (String) session.getAttribute("username");
        if (!username.equals(loginUser)) {
            return Result.error("-1", "无权限查询他人预约");
        }

        // 未来3天日期范围（含今天）
        LocalDate today = LocalDate.now();
        LocalDate threeDaysLater = today.plusDays(3);

        QueryWrapper<ClassroomReservation> query = new QueryWrapper<>();
        query.eq("username", username)
                .between("reserve_date", today, threeDaysLater)
                .orderByAsc("reserve_date", "start_time");

        List<ClassroomReservation> reservations = reservationService.list(query);
        return Result.success(reservations);
    }
    // 在 ClassroomReservationController 中补充
    @GetMapping("/userTodayCount")
    public Result<?> getUserTodayReservationCount(@RequestParam String username) {
        try {
            LocalDate today = LocalDate.now();
            QueryWrapper<ClassroomReservation> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", username) // 过滤当前用户
                    .eq("reserve_date", today); // 过滤今日
            long count = reservationService.count(queryWrapper);
            return Result.success(count);
        } catch (Exception e) {
            return Result.error("-1", "统计个人今日预约次数失败：" + e.getMessage());
        }
    }
    private static final Logger log = LoggerFactory.getLogger(ClassroomReservationController.class);
    @GetMapping("/myCount")
    public Result<?> getMyReservationCount(HttpSession session) {
        String username = (String) session.getAttribute("username");
        String identity = (String) session.getAttribute("identity");

        // 登录状态校验
        if (username == null || identity == null) {
            return Result.error("-1", "请先登录");
        }

        try {
            // 实际业务中替换为service查询
            // 状态说明：0=待审核，1=已通过，2=已拒绝，3=已过期
            QueryWrapper<ClassroomReservation> pendingQw = new QueryWrapper<>();
            pendingQw.eq("username", username)
                    .eq("check_status", 0);
            long pending = reservationService.count(pendingQw);

            QueryWrapper<ClassroomReservation> approvedQw = new QueryWrapper<>();
            approvedQw.eq("username", username)
                    .eq("check_status", 1);
            long approved = reservationService.count(approvedQw);

            Map<String, Long> countMap = new HashMap<>();
            countMap.put("pending", pending);
            countMap.put("approved", approved);
            return Result.success(countMap);
        } catch (Exception e) {
            log.error("查询预约统计失败", e);
            return Result.error("-1", "查询失败");
        }
    }
    /**
     * 统计用户的签到/签退状态数量
     * （待签到：checkStatus=0；已签到：checkStatus=1；已完成：checkStatus=2）
     */
    @GetMapping("/checkInOutCount")
    public Result<?> getCheckInOutCount(HttpSession session) {  // 移除username参数
        // 1. 从session获取当前登录用户（无需前端传递）
        String username = (String) session.getAttribute("username");
        if (username == null || username.trim().isEmpty()) {
            return Result.error("-1", "请先登录");
        }

        try {
            // 2. 统计各状态数量（使用session中的username）
            QueryWrapper<ClassroomReservation> query = new QueryWrapper<>();
            query.eq("username", username);

            // 待签到（已通过审核但未签到）
            long pendingCheckinCount = reservationService.count(
                    query.clone().eq("status", 1).eq("check_status", 0)
            );

            // 已签到（未签退）
            long checkedInCount = reservationService.count(
                    query.clone().eq("check_status", 1)
            );

            // 已完成（已签退）
            long completedCount = reservationService.count(
                    query.clone().eq("check_status", 2)
            );

            // 3. 封装结果
            Map<String, Long> countMap = new HashMap<>();
            countMap.put("pendingCheckinCount", pendingCheckinCount);
            countMap.put("checkedInCount", checkedInCount);
            countMap.put("completedCount", completedCount);

            return Result.success(countMap);
        } catch (Exception e) {
            log.error("统计签到签退数据失败", e);
            return Result.error("-1", "统计签到签退数据失败：" + e.getMessage());
        }
    }

}