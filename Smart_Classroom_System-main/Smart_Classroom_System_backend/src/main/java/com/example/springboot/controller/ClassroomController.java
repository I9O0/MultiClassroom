package com.example.springboot.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Classroom;
import com.example.springboot.entity.ClassroomReservation;
import com.example.springboot.service.ClassroomReservationService;
import com.example.springboot.service.ClassroomService;
import com.example.springboot.unit.ClassroomPermissionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 教室Controller（接口入口）
 */
@RestController
@RequestMapping("/classroom")
public class ClassroomController {
    private static final Logger log = LoggerFactory.getLogger(ClassroomController.class);
    @Resource
    private ClassroomReservationService reservationService;
    @Autowired
    private ClassroomService classroomService;

    // 统一日期格式化器
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * 教室查询接口（查询7天内的教室，无需传递queryDate）
     */
    @GetMapping("/list")
    public Result getClassroomList(
            @RequestParam(required = false) String buildingId,
            @RequestParam(required = false) String queryDate,
            HttpSession session) {
        try {
            String identity = (String) session.getAttribute("identity");
            if (identity == null) {
                return Result.error("-1", "请先登录");
            }

            String errorMsg = ClassroomPermissionUtil.checkPermission(identity, buildingId);
            if (errorMsg != null) {
                return Result.error("-1", errorMsg);
            }

            List<Classroom> classrooms;
            if (queryDate != null && !queryDate.isEmpty()) {
                // 按指定日期和教学楼筛选
                classrooms = classroomService.listByBuildingAndDate(buildingId, queryDate);
            } else {
                // 默认查询7天内的教室
                classrooms = classroomService.getClassroomsWithReserveStatus(buildingId);
            }
            return Result.success(classrooms);

        } catch (Exception e) {
            return Result.error("-1", "查询教室失败：" + e.getMessage());
        }
    }

    /**
     * 查询教室可用时间段
     */
    @GetMapping("/availableTimeSlots")
    public Result getAvailableTimeSlots(
            @RequestParam String classroomId,
            @RequestParam String date,
            @RequestParam String buildingId,
            HttpSession session) {
        try {
            // 1. 验证登录状态
            String identity = (String) session.getAttribute("identity");
            if (identity == null) {
                return Result.error("-1", "请先登录");
            }

            // 2. 权限校验
            String errorMsg = ClassroomPermissionUtil.checkPermission(identity, buildingId);
            if (errorMsg != null) {
                return Result.error("-1", errorMsg);
            }

            // 3. 参数校验
            if (classroomId == null || classroomId.trim().isEmpty() ||
                    date == null || date.trim().isEmpty() ||
                    buildingId == null || buildingId.trim().isEmpty()) {
                return Result.error("-1", "教室ID、日期和教学楼ID不能为空");
            }

            // 验证日期格式
            LocalDate.parse(date, DATE_FORMATTER);
            List<String> timeSlots = classroomService.getAvailableTimeSlots(classroomId, date);
            return Result.success(timeSlots);
        } catch (DateTimeParseException e) {
            return Result.error("-1", "日期格式错误，正确格式为yyyy-MM-dd");
        } catch (Exception e) {
            return Result.error("-1", "查询可用时间段失败：" + e.getMessage());
        }
    }

    /**
     * 管理员查询所有教室
     */
    @GetMapping("/admin/listAll")
    public Result getAllClassrooms(HttpSession session) {
        String identity = (String) session.getAttribute("identity");
        // 允许admin和manager访问
        if (!"admin".equals(identity) && !"manager".equals(identity)) {
            return Result.error("-1", "无权限：仅超级管理员和管理员可查看所有教室");
        }
        List<Classroom> allClassrooms = classroomService.list();
        return Result.success(allClassrooms);
    }

    /**
     * 管理员修改教室信息
     */
    @PutMapping("/admin/update")
    public Result updateClassroom(
            @RequestBody Classroom classroom,
            HttpSession session) {
        String identity = (String) session.getAttribute("identity");
        if (!ClassroomPermissionUtil.isAdminOrmanager(identity)) {
            return Result.error("-1", "无权限：仅超级管理员和管理员可修改教室信息");
        }
        if (classroom.getId() == null) {
            return Result.error("-1", "教室ID不能为空");
        }
        boolean success = classroomService.updateById(classroom);
        return success ? Result.success("修改成功") : Result.error("-1", "修改失败：教室不存在");
    }

    /**
     * 管理员修改教室状态（启用/禁用）
     */
    @PutMapping("/admin/status")
    public Result updateClassroomStatus(
            @RequestParam Integer id,
            @RequestParam Integer status,
            HttpSession session) {
        String identity = (String) session.getAttribute("identity");
        if (!ClassroomPermissionUtil.isAdminOrmanager(identity)) {
            return Result.error("-1", "无权限：仅超级管理员和管理员可修改教室信息");
        }
        if (status != 0 && status != 1) {
            return Result.error("-1", "状态值错误：0-禁用，1-启用");
        }
        Classroom classroom = new Classroom();
        classroom.setId(id);
        classroom.setStatus(status);
        boolean success = classroomService.updateById(classroom);
        return success ? Result.success("状态已更新为" + (status == 1 ? "启用" : "禁用"))
                : Result.error("-1", "修改失败：教室不存在");
    }

    /**
     * 管理员新增教室
     */
    @PostMapping("/admin/add")
    public Result addClassroom(
            @RequestBody Classroom classroom,
            HttpSession session) {
        String identity = (String) session.getAttribute("identity");
        if (!ClassroomPermissionUtil.isAdminOrmanager(identity)) {
            return Result.error("-1", "无权限：仅超级管理员和管理员可修改教室信息");
        }
        if (classroom.getClassroomId() == null || classroom.getBuildingId() == null) {
            return Result.error("-1", "教室号和教学楼ID不能为空");
        }
        if (classroom.getStatus() == null) {
            classroom.setStatus(1); // 默认可用
        }
        boolean success = classroomService.save(classroom);
        return success ? Result.success("新增成功") : Result.error("-1", "新增失败：数据已存在");
    }

    /**
     * 管理员删除教室
     */
    @DeleteMapping("/admin/delete/{id}")
    public Result deleteClassroom(
            @PathVariable Integer id,
            HttpSession session) {
        String identity = (String) session.getAttribute("identity");
        if (!ClassroomPermissionUtil.isAdminOrmanager(identity)) {
            return Result.error("-1", "无权限：仅超级管理员和管理员可修改教室信息");
        }
        boolean success = classroomService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("-1", "删除失败：教室不存在");
    }

    /**
     * 根据教学楼ID查询教室列表（用于报修时的下拉选择）
     */
    @GetMapping("/listByBuilding")
    public Result getClassroomsByBuilding(
            @RequestParam String buildingId,
            @RequestParam(required = false) String date,
            HttpSession session) {
        try {
            String identity = (String) session.getAttribute("identity");
            if (identity == null) {
                return Result.error("-1", "请先登录");
            }

            // 权限校验
            String errorMsg = ClassroomPermissionUtil.checkPermission(identity, buildingId);
            if (errorMsg != null) {
                return Result.error("-1", errorMsg);
            }

            // 日期默认取当天（使用java.time包避免线程安全问题）
            if (date == null || date.trim().isEmpty()) {
                date = LocalDate.now().format(DATE_FORMATTER);
            }

            // 查询该教学楼+当天的可用教室（status=1）
            QueryWrapper<Classroom> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("building_id", buildingId)
                    .eq("date", date)
                    .eq("status", 1);

            // 按building_id+classroom_id去重
            List<Classroom> classrooms = classroomService.list(queryWrapper);
            Map<String, Classroom> uniqueClassrooms = new HashMap<>();
            for (Classroom classroom : classrooms) {
                String key = classroom.getBuildingId() + "-" + classroom.getClassroomId();
                uniqueClassrooms.putIfAbsent(key, classroom);
            }

            return Result.success(new ArrayList<>(uniqueClassrooms.values()));

        } catch (Exception e) {
            return Result.error("-1", "查询教室失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有可用教学楼列表（用于报修时选择）
     */
    @GetMapping("/buildings")
    public Result<?> getClassroomBuildings(HttpSession session) {
        String identity = (String) session.getAttribute("identity");
        if (identity == null) {
            return Result.error("-1", "请先登录");
        }

        String defaultBuildings = ClassroomPermissionUtil.getDefaultBuildings(identity);
        if (defaultBuildings.isEmpty()) {
            return Result.error("-1", "无可用教学楼信息");
        }

        // 构造教学楼列表（ID+名称）
        List<Map<String, String>> buildingList = Arrays.stream(defaultBuildings.split(","))
                .map(buildingId -> {
                    Map<String, String> building = new HashMap<>();
                    building.put("id", buildingId);
                    building.put("name", buildingId + "教学楼");
                    return building;
                })
                .collect(Collectors.toList());

        return Result.success(buildingList);
    }

    /**
     * 统计权限范围内可用教室总数
     */
    @GetMapping("/total")
    public Result<?> getClassroomTotal(HttpSession session) {
        String identity = (String) session.getAttribute("identity");
        if (identity == null) {
            return Result.error("-1", "请先登录");
        }

        try {
            // 获取用户有权限的教学楼
            String visibleBuildings = ClassroomPermissionUtil.getVisibleBuildings(identity);
            List<String> buildingIds = Arrays.asList(visibleBuildings.split(","));

            // 查询权限范围内的可用教室并去重
            QueryWrapper<Classroom> qw = new QueryWrapper<>();
            qw.eq("status", 1)
                    .in("building_id", buildingIds);

            Set<String> uniqueClassroomNames = classroomService.list(qw).stream()
                    .map(Classroom::getClassroomName)
                    .collect(Collectors.toSet());

            return Result.success(uniqueClassroomNames.size());
        } catch (Exception e) {
            return Result.error("-1", "获取教室数量失败：" + e.getMessage());
        }
    }

    /**
     * 当日可用教室总数（排除完全预约的教室）
     */
    @GetMapping("/available/today")
    public Result<?> getTodayAvailableTotal(
            @RequestParam String date,
            HttpSession session) {
        String identity = (String) session.getAttribute("identity");
        if (identity == null) {
            return Result.error("-1", "请先登录");
        }

        try {
            LocalDate targetDate = LocalDate.parse(date, DATE_FORMATTER);
            List<Classroom> allAvailableList = classroomService.list(new QueryWrapper<Classroom>().eq("status", 1));

            // 筛选日期有效且未完全预约的教室
            List<Classroom> validClassrooms = filterValidClassrooms(allAvailableList, targetDate, date);

            return Result.success(validClassrooms.size());
        } catch (DateTimeParseException e) {
            return Result.error("-1", "日期格式错误，正确格式为yyyy-MM-dd");
        } catch (Exception e) {
            return Result.error("-1", "获取当日可用教室数失败：" + e.getMessage());
        }
    }

    /**
     * 获取当前可用教室数量（排除当前时间已被预约的教室）
     */
    @GetMapping("/available/current")
    public Result<?> getCurrentAvailableCount() {
        long count = classroomService.getCurrentAvailableCount();
        return Result.success(count);
    }

    /**
     * 查询所有可用教室（status=1）
     */
    @GetMapping("/all")
    public Result<?> getAllAvailableClassrooms() {
        QueryWrapper<Classroom> query = new QueryWrapper<>();
        query.eq("status", 1);
        List<Classroom> list = classroomService.list(query);
        return Result.success(list);
    }

    /**
     * 按教学楼统计权限范围内的可用教室数量
     */
    @GetMapping("/countByBuilding")
    public Result<?> countByBuilding(HttpSession session) {
        String identity = (String) session.getAttribute("identity");
        if (identity == null) {
            return Result.error("-1", "请先登录");
        }

        try {
            String visibleBuildings = ClassroomPermissionUtil.getVisibleBuildings(identity);
            List<String> buildingIds = Arrays.asList(visibleBuildings.split(","));

            // 按楼栋分组统计
            List<Map<String, Object>> countList = classroomService.listMaps(
                    new QueryWrapper<Classroom>()
                            .eq("status", 1)
                            .in("building_id", buildingIds)
                            .select("building_id", "count(*) as value")
                            .groupBy("building_id")
            );

            // 转换为图表格式
            List<Map<String, Object>> result = countList.stream().map(map -> {
                Map<String, Object> newMap = new HashMap<>();
                newMap.put("name", map.get("building_id"));
                newMap.put("value", map.get("value"));
                return newMap;
            }).collect(Collectors.toList());

            return Result.success(result);
        } catch (Exception e) {
            return Result.error("-1", "统计失败：" + e.getMessage());
        }
    }

    /**
     * 按教学楼统计权限范围内的可用教室总数（带权限控制）
     */
    @GetMapping("/totalByBuilding")
    public Result<?> getTotalByBuilding(HttpSession session) {
        String identity = (String) session.getAttribute("identity");
        if (identity == null) {
            return Result.error("-1", "请先登录");
        }

        try {
            // 获取用户有权限的教学楼
            String visibleBuildings = ClassroomPermissionUtil.getVisibleBuildings(identity);
            List<String> buildingIds = Arrays.asList(visibleBuildings.split(","));

            // 统计权限范围内的可用教室
            List<Classroom> allClassrooms = classroomService.list(
                    new QueryWrapper<Classroom>()
                            .eq("status", 1)
                            .in("building_id", buildingIds)
            );

            Map<String, Long> totalMap = allClassrooms.stream()
                    .collect(Collectors.groupingBy(Classroom::getBuildingId, Collectors.counting()));
            return Result.success(totalMap);
        } catch (Exception e) {
            return Result.error("-1", "统计失败：" + e.getMessage());
        }
    }

    /**
     * 当日可用教室按楼栋分布
     */
    @GetMapping("/available/building")
    public Result<?> getTodayAvailableByBuilding(
            @RequestParam String date,
            HttpSession session) {
        String identity = (String) session.getAttribute("identity");
        if (identity == null) {
            return Result.error("-1", "请先登录");
        }

        try {
            LocalDate targetDate = LocalDate.parse(date, DATE_FORMATTER);
            List<Classroom> allAvailableList = classroomService.list(new QueryWrapper<Classroom>().eq("status", 1));

            // 筛选日期有效且未完全预约的教室
            List<Classroom> validClassrooms = filterValidClassrooms(allAvailableList, targetDate, date);

            // 按楼栋分组统计
            Map<String, Long> buildingCountMap = validClassrooms.stream()
                    .collect(Collectors.groupingBy(Classroom::getBuildingId, Collectors.counting()));

            // 转换为图表格式
            List<Map<String, Object>> resultList = buildingCountMap.entrySet().stream()
                    .map(entry -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("name", entry.getKey());
                        map.put("value", entry.getValue());
                        return map;
                    })
                    .collect(Collectors.toList());

            return Result.success(resultList);
        } catch (DateTimeParseException e) {
            return Result.error("-1", "日期格式错误，正确格式为yyyy-MM-dd");
        } catch (Exception e) {
            return Result.error("-1", "获取教室楼栋分布失败：" + e.getMessage());
        }
    }

    /**
     * 筛选日期有效且未完全预约的教室（抽取公共逻辑）
     */
    private List<Classroom> filterValidClassrooms(List<Classroom> allClassrooms, LocalDate targetDate, String dateStr) {
        // 1. 筛选日期在有效区间内的教室
        List<Classroom> dateValidList = allClassrooms.stream()
                .filter(classroom -> isDateInRange(classroom, targetDate))
                .collect(Collectors.toList());

        // 2. 按教室名称去重
        Map<String, Classroom> uniqueMap = dateValidList.stream()
                .collect(Collectors.toMap(
                        Classroom::getClassroomName,
                        Function.identity(),
                        (v1, v2) -> v1
                ));
        List<Classroom> uniqueList = new ArrayList<>(uniqueMap.values());

        // 3. 排除完全预约的教室
        return uniqueList.stream()
                .filter(classroom -> isNotFullyBooked(classroom.getClassroomId(), dateStr))
                .collect(Collectors.toList());
    }

    /**
     * 判断教室日期是否在目标日期区间内
     */
    private boolean isDateInRange(Classroom classroom, LocalDate targetDate) {
        LocalDate createLocalDate = classroom.getCreateTime()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate endLocalDate = LocalDate.parse(classroom.getDate(), DATE_FORMATTER);
        return !targetDate.isBefore(createLocalDate) && !targetDate.isAfter(endLocalDate);
    }

    /**
     * 判断教室是否未被完全预约（8:00-22:00）
     */
    private boolean isNotFullyBooked(String classroomId, String date) {
        QueryWrapper<ClassroomReservation> qw = new QueryWrapper<>();
        qw.eq("classroom_id", classroomId)
                .eq("reserve_date", date)
                .eq("status", 1); // 仅统计已通过的预约
        List<ClassroomReservation> reservations = reservationService.list(qw);

        // 无预约 → 可用
        if (reservations.isEmpty()) {
            return true;
        }

        // 检查是否存在覆盖全天的预约
        return reservations.stream()
                .noneMatch(res -> "08:00".equals(res.getStartTime()) && "22:00".equals(res.getEndTime()));
    }
    /**
     * 本人可预约楼栋当日可用教室文案
     */
    @GetMapping("/available/myBuilding")
    public Result<?> getMyBuildingAvailable(
            @RequestParam String date,
            HttpSession session) {
        String identity = (String) session.getAttribute("identity");

        if (identity == null) {
            return Result.error("-1", "请先登录");
        }

        try {
            // 验证日期格式
            LocalDate.parse(date, DATE_FORMATTER);

            // 获取用户可访问的教学楼
            String defaultBuildings = ClassroomPermissionUtil.getDefaultBuildings(identity);
            if (defaultBuildings.isEmpty()) {
                return Result.success("暂无可用教室");
            }

            // 实际业务中替换为service查询各楼栋可用数量
            Map<String, Integer> buildingCount = new HashMap<>();
            for (String buildingId : defaultBuildings.split(",")) {
                // 模拟数据，实际需查询该楼栋当日可用教室数
                buildingCount.put(buildingId, new Random().nextInt(5) + 1);
            }

            // 格式化返回文本（如：J1:3间 | J2:5间）
            StringJoiner sj = new StringJoiner(" | ");
            buildingCount.forEach((id, count) -> sj.add(id + ":" + count + "间"));
            return Result.success(sj.toString());
        } catch (DateTimeParseException e) {
            return Result.error("-1", "日期格式错误，正确格式为yyyy-MM-dd");
        } catch (Exception e) {
            log.error("查询可用教室失败", e);
            return Result.error("-1", "查询失败");
        }
    }

    /**
     * 本人可预约楼栋可用教室图表数据
     */
    @GetMapping("/available/myBuildingChart")
    public Result<?> getMyBuildingChart(
            @RequestParam String date,
            HttpSession session) {
        String identity = (String) session.getAttribute("identity");

        if (identity == null) {
            return Result.error("-1", "请先登录");
        }

        try {
            // 验证日期格式
            LocalDate.parse(date, DATE_FORMATTER);

            // 获取用户可访问的教学楼
            String defaultBuildings = ClassroomPermissionUtil.getDefaultBuildings(identity);
            if (defaultBuildings.isEmpty()) {
                return Result.success(Collections.emptyList());
            }

            // 实际业务中替换为service查询数据
            List<Map<String, Object>> chartData = new ArrayList<>();
            for (String buildingId : defaultBuildings.split(",")) {
                // 模拟数据，实际需查询该楼栋当日可用教室数
                Map<String, Object> item = new HashMap<>();
                item.put("name", buildingId + "教学楼");
                item.put("value", new Random().nextInt(5) + 1);
                chartData.add(item);
            }

            return Result.success(chartData);
        } catch (DateTimeParseException e) {
            return Result.error("-1", "日期格式错误，正确格式为yyyy-MM-dd");
        } catch (Exception e) {
            log.error("查询图表数据失败", e);
            return Result.error("-1", "查询失败");
        }
    }
    
}