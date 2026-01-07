package com.example.springboot.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.ClassroomRepair;
import com.example.springboot.service.ClassroomRepairService;
import com.example.springboot.service.StudentService;
import com.example.springboot.service.TeacherService;
import com.example.springboot.unit.ClassroomPermissionUtil;
import com.example.springboot.unit.FileUploadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 整合：教室报修 + 图片上传 控制器（修复 log 和 Result 方法错误）
 */
@RestController
@RequestMapping("/repair/classroom") // 保持原有前缀不变
public class ClassroomRepairController {

    // 修复1：手动创建 Logger 实例（无需 Lombok @Slf4j）
    private static final Logger log = LoggerFactory.getLogger(ClassroomRepairController.class);

    // 状态常量定义（统一管理，避免硬编码）
    private static final Integer STATUS_PENDING = 0;    // 未处理
    private static final Integer STATUS_REPAIRING = 1;  // 处理中
    private static final Integer STATUS_SOLVED = 2;     // 已解决

    @Autowired
    private ClassroomRepairService classroomRepairService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private FileUploadUtil fileUploadUtil;

    // ====================== 图片上传相关接口（修复 Result.success 方法调用）======================
    /**
     * 报修图片上传（支持jpg/png，限制5MB，需登录）
     * 完整路径：/repair/classroom/upload/repair
     */
    // ====================== 图片上传接口（单参数返回文件路径）======================
    @PostMapping("/upload/repair")
    public Result<?> uploadRepairImage(@RequestParam("file") MultipartFile file, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return Result.error("-1", "请先登录再上传图片");
        }

        try {
            // 文件类型校验
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("-1", "请上传jpg/png格式的图片文件");
            }

            // 文件大小校验（5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                return Result.error("-1", "文件大小不能超过5MB");
            }

            // 上传文件获取路径
            String filePath = fileUploadUtil.uploadFile(file);

            // 单参数返回：仅返回文件路径（前端通过 response.data 获取）
            return Result.success(filePath);
        } catch (Exception e) {
            log.error("图片上传失败，用户名：{}", username, e);
            return Result.error("-1", "图片上传失败：" + e.getMessage());
        }
    }
    /**
     * 学生/老师提交教室报修（需登录，权限由ClassroomPermissionUtil控制）
     */
    @PostMapping("/submit")
    public Result<?> submitRepair(@RequestBody ClassroomRepair repair, HttpSession session) {
        String identity = (String) session.getAttribute("identity");
        String username = (String) session.getAttribute("username");

        // 1. 校验登录状态
        if (username == null || identity == null) {
            return Result.error("-1", "请先登录");
        }

        // 2. 提交权限校验（仅学生/老师可提交）
        if (!ClassroomPermissionUtil.canSubmitRepair(identity)) {
            return Result.error("-1", "无权限：仅学生和老师可提交报修");
        }

        // 3. 教学楼权限校验（学生J1/J2，教师J3/J4，管理员/经理无限制）
        String buildingId = repair.getBuildingId();
        String permissionError = ClassroomPermissionUtil.checkPermission(identity, buildingId);
        if (permissionError != null) {
            return Result.error("-1", permissionError);
        }

        // 4. 参数校验
        if (buildingId == null || buildingId.trim().isEmpty() ||
                repair.getClassroomId() == null || repair.getClassroomId().trim().isEmpty() ||
                repair.getDeviceType() == null || repair.getDeviceType().trim().isEmpty() ||
                repair.getProblemDetail() == null || repair.getProblemDetail().trim().isEmpty()) {
            return Result.error("-1", "教学楼、教室号、设备类型和问题描述不能为空");
        }

        // 5. 补充报修人信息
        repair.setReporterId(username);
        String reporterName = (String) session.getAttribute("name");
        repair.setReporterName(reporterName);
        repair.setReporterRole(identity);
        repair.setStatus(STATUS_PENDING);
        repair.setSubmitTime(LocalDateTime.now());

        // 6. 保存数据
        boolean success = classroomRepairService.save(repair);
        return success ? Result.success("报修提交成功") : Result.error("-1", "提交失败");
    }

    /**
     * 超级管理员/管理员查询待处理报修（分页，权限由ClassroomPermissionUtil控制）
     */
    @GetMapping("/pending")
    public Result<?> getPendingRepairs(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String buildingId,
            HttpSession session) {

        String identity = (String) session.getAttribute("identity");
        // 权限校验：仅admin和manager可查看（两者权限一致）
        if (!ClassroomPermissionUtil.canHandleRepair(identity)) {
            return Result.error("-1", "无权限：仅超级管理员和管理员可查看");
        }

        Page<ClassroomRepair> page = new Page<>(pageNum, pageSize);
        QueryWrapper<ClassroomRepair> query = new QueryWrapper<>();
        query.eq("status", STATUS_PENDING)
                .eq(buildingId != null && !buildingId.isEmpty(), "building_id", buildingId)
                .orderByDesc("submit_time");

        Page<ClassroomRepair> result = classroomRepairService.page(page, query);
        return Result.success(result);
    }

    /**
     * 处理报修（更新状态和结果，仅admin和manager可操作）
     */
    @PutMapping("/handle")
    public Result<?> handleRepair(@RequestBody Map<String, Object> params, HttpSession session) {
        String identity = (String) session.getAttribute("identity");
        // 权限校验：admin和manager权限一致
        if (!ClassroomPermissionUtil.canHandleRepair(identity)) {
            return Result.error("-1", "无权限：仅管理员和教室超级管理员可处理");
        }

        // 参数校验
        if (params.get("id") == null || params.get("status") == null || params.get("handleResult") == null) {
            return Result.error("-1", "报修ID、处理状态和处理结果不能为空");
        }

        Integer id;
        try {
            id = Integer.parseInt(params.get("id").toString());
        } catch (NumberFormatException e) {
            return Result.error("-1", "报修ID格式错误");
        }

        // 状态转换（前端字符串 → 后端整数）
        String statusStr = params.get("status").toString();
        Integer status;
        if ("repairing".equals(statusStr)) {
            status = STATUS_REPAIRING; // 1-处理中
        } else if ("solved".equals(statusStr)) {
            status = STATUS_SOLVED;    // 2-已解决
        } else {
            return Result.error("-1", "状态值错误：只能是repairing或solved");
        }

        String handleResult = params.get("handleResult").toString().trim();
        if (handleResult.isEmpty()) {
            return Result.error("-1", "处理结果不能为空");
        }

        // 获取处理人信息
        String handlerId = (String) session.getAttribute("username");
        String handlerName = (String) session.getAttribute("name");
        if (handlerId == null || handlerName == null) {
            return Result.error("-1", "处理人信息获取失败，请重新登录");
        }

        ClassroomRepair repair = classroomRepairService.getById(id);
        if (repair == null) {
            return Result.error("-1", "报修记录不存在");
        }

        // 更新报修信息
        repair.setStatus(status);
        repair.setHandleResult(handleResult);
        repair.setHandlerId(handlerId);
        repair.setHandlerName(handlerName);
        repair.setHandleTime(LocalDateTime.now());

        boolean success = classroomRepairService.updateById(repair);
        return success ? Result.success("处理成功") : Result.error("-1", "处理失败");
    }

    /**
     * 个人报修记录查询（学生/老师查询自己的报修）
     */
    @GetMapping("/myRecords")
    public Result<?> getMyRecords(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpSession session) {
        String username = (String) session.getAttribute("username");
        String identity = (String) session.getAttribute("identity");

        if (username == null || identity == null) {
            return Result.error("-1", "请先登录");
        }

        Page<ClassroomRepair> page = new Page<>(pageNum, pageSize);
        QueryWrapper<ClassroomRepair> query = new QueryWrapper<>();
        query.eq("reporter_id", username)
                .orderByDesc("submit_time");

        Page<ClassroomRepair> result = classroomRepairService.page(page, query);
        return Result.success(result);
    }

    /**
     * 超级管理员/管理员查询所有报修记录（带筛选）
     */
    @GetMapping("/admin/all")
    public Result<?> getAllRepairs(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String buildingId,
            HttpSession session) {

        String identity = (String) session.getAttribute("identity");
        if (!ClassroomPermissionUtil.canHandleRepair(identity)) {
            return Result.error("-1", "无权限：仅超级管理员和管理员可查看");
        }

        Page<ClassroomRepair> page = new Page<>(pageNum, pageSize);
        QueryWrapper<ClassroomRepair> query = new QueryWrapper<>();
        query.eq(status != null, "status", status)
                .eq(buildingId != null && !buildingId.isEmpty(), "building_id", buildingId)
                .orderByDesc("submit_time");

        Page<ClassroomRepair> result = classroomRepairService.page(page, query);
        return Result.success(result);
    }

    /**
     * 按状态统计报修数量（仅admin和manager可查看）
     */
    @GetMapping("/countByStatus")
    public Result<?> countByStatus(HttpSession session) {
        String identity = (String) session.getAttribute("identity");
        if (!ClassroomPermissionUtil.canHandleRepair(identity)) {
            return Result.error("-1", "无权限：仅超级管理员和管理员可查看统计数据");
        }
        Map<String, Integer> countMap = classroomRepairService.countByStatus();
        return Result.success(countMap);
    }

    /**
     * 按教学楼统计待处理报修数量（仅admin和manager可查看）
     */
    @GetMapping("/countPendingByBuilding")
    public Result<?> countPendingByBuilding(HttpSession session) {
        String identity = (String) session.getAttribute("identity");
        if (!ClassroomPermissionUtil.canHandleRepair(identity)) {
            return Result.error("-1", "无权限：仅超级管理员和管理员可查看统计数据");
        }
        Map<String, Integer> buildingCountMap = classroomRepairService.countPendingByBuilding(STATUS_PENDING);
        return Result.success(buildingCountMap);
    }

    /**
     * 获取当前用户可见的教学楼（适配前端下拉框渲染，调用ClassroomPermissionUtil工具类）
     */
    @GetMapping("/getVisibleBuildings")
    public Result<?> getVisibleBuildings(HttpSession session) {
        String identity = (String) session.getAttribute("identity");
        if (identity == null) {
            return Result.error("-1", "请先登录");
        }
        // 调用工具类获取默认可见教学楼（逗号分隔字符串）
        String visibleBuildings = ClassroomPermissionUtil.getDefaultBuildings(identity);
        // 转换为数组返回给前端
        String[] buildingArray = visibleBuildings.split(",");
        return Result.success(buildingArray);
    }
    @GetMapping("/myPendingCount")
    public Result<?> getMyPendingRepairCount(HttpSession session) {
        String username = (String) session.getAttribute("username");

        if (username == null) {
            return Result.error("-1", "请先登录");
        }

        try {
            // 待跟进状态：未处理(0) + 处理中(1)
            QueryWrapper<ClassroomRepair> query = new QueryWrapper<>();
            query.eq("reporter_id", username)
                    .in("status", STATUS_PENDING, STATUS_REPAIRING);

            long pendingCount = classroomRepairService.count(query);
            return Result.success(pendingCount);
        } catch (Exception e) {
            log.error("查询报修待跟进数失败", e);
            return Result.error("-1", "查询失败");
        }
    }

}