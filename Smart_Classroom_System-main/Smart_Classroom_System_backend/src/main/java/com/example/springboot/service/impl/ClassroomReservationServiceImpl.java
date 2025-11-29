package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.common.Result;
import com.example.springboot.entity.*;
import com.example.springboot.mapper.ClassroomMapper;
import com.example.springboot.mapper.ClassroomReservationMapper;
import com.example.springboot.service.ClassroomReservationService;
import com.example.springboot.service.StudentService;
import com.example.springboot.service.TeacherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClassroomReservationServiceImpl extends ServiceImpl<ClassroomReservationMapper, ClassroomReservation> implements ClassroomReservationService {

    private static final Logger log = LoggerFactory.getLogger(ClassroomReservationServiceImpl.class);
    private static final SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm");

    @Autowired
    private ClassroomMapper classroomMapper;
    @Autowired
    private TeacherService teacherService;  // 注入教师Service

    @Autowired
    private StudentService studentService;  // 注入学生Service
    @Override
    public Result<?> submitReservation(ClassroomReservation reservation) {
        try {
            String identity = reservation.getIdentity();
            // 1. 基础参数校验
            if ("teacher".equals(identity)) {
                // 教师：查询教师表获取姓名
                Teacher teacher = teacherService.getById(reservation.getUsername());
                if (teacher == null) {
                    return Result.error("-1", "教师信息不存在");
                }
                reservation.setReserverName(teacher.getName()); // 设置教师姓名
            } else {
                // 学生：查询学生表获取姓名
                Student student = studentService.getById(reservation.getUsername());
                if (student == null) {
                    return Result.error("-1", "学生信息不存在");
                }
                reservation.setReserverName(student.getName()); // 设置学生姓名
            }
            if (reservation.getBuildingId() == null || reservation.getBuildingId().trim().isEmpty()) {
                return Result.error("-1", "教学楼ID不能为空");
            }
            if (reservation.getReserveReason() == null || reservation.getReserveReason().trim().isEmpty()) {
                return Result.error("-1", "预约理由不能为空");
            }

            // 2. 校验预约日期（7天内）
            LocalDate today = LocalDate.now();
            LocalDate reserveDate = reservation.getReserveDate();

            LocalDate maxReserveDate = today.plusDays(6); // 7天内可预约

            if (reserveDate.isBefore(today) || reserveDate.isAfter(maxReserveDate)) {
                return Result.error("-1", "只能预约7天内的教室");
            }

            // 3. 一周内预约次数限制（区分学生和教师，基于已完成的有效预约）
            String userIdentity = reservation.getIdentity();
            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);

// 统计已完成的有效预约（已签退状态）
            QueryWrapper<ClassroomReservation> completedQuery = new QueryWrapper<>();
            completedQuery.eq("username", reservation.getUsername())
                    .ge("create_time", weekAgo)
                    .eq("check_status", 2); // 仅统计已签退的完成状态

            long completedCount = baseMapper.selectCount(completedQuery);

// 教师额外检查未完成的预约（未签退或未签到）
            if ("teacher".equals(userIdentity)) {
                QueryWrapper<ClassroomReservation> unfinishedQuery = new QueryWrapper<>();
                unfinishedQuery.eq("username", reservation.getUsername())
                        .in("check_status", 0, 1) // 未签到或已签到但未签退
                        .in("status", 1); // 已通过审核的未完成预约

                long unfinishedCount = baseMapper.selectCount(unfinishedQuery);

                // 教师限制：存在未完成预约 或 已完成次数达3次 均不可再预约
                if (unfinishedCount > 0) {
                    return Result.error("-1", "您有未完成的预约，请完成签退后再申请");
                }
                if (completedCount >= 3) {
                    return Result.error("-1", "教师一周内最多只能完成3次预约，请下周再尝试");
                }
            } else if ("stu".equals(userIdentity)) {
                // 学生限制：已完成次数达1次不可再预约
                if (completedCount >= 1) {
                    return Result.error("-1", "学生一周内只能完成1次预约，请下周再尝试");
                }
            } else {
                return Result.error("-1", "身份异常，无法预约");
            }

            // 4. 校验时段合法性
            String startTimeStr = reservation.getStartTime();
            String endTimeStr = reservation.getEndTime();
            if (startTimeStr == null || endTimeStr == null
                    || !startTimeStr.matches("^([01]\\d|2[0-3]):([0-5]\\d)$")
                    || !endTimeStr.matches("^([01]\\d|2[0-3]):([0-5]\\d)$")) {
                return Result.error("-1", "时间格式错误，正确格式为HH:mm");
            }

            Date startTime = timeSdf.parse(startTimeStr);
            Date endTime = timeSdf.parse(endTimeStr);
            Date startLimit = timeSdf.parse("08:00");
            Date endLimit = timeSdf.parse("22:00");
            Date noonStart = timeSdf.parse("12:00");
            Date noonEnd = timeSdf.parse("14:00");

            if (startTime.before(startLimit) || endTime.after(endLimit)) {
                return Result.error("-1", "预约时间必须在8:00-22:00之间");
            }
            if (startTime.before(noonEnd) && endTime.after(noonStart)) {
                return Result.error("-1", "12:00-14:00为午休时间，不可预约");
            }
            long durationMinutes = (endTime.getTime() - startTime.getTime()) / (60 * 1000);
            if (durationMinutes != 120) {
                return Result.error("-1", "每次预约时长必须为2小时");
            }

            // 5. 校验教室可用性
            String classroomId = reservation.getClassroomId();
            String reserveDateStr = reserveDate.toString(); // LocalDate默认格式为yyyy-MM-dd

            QueryWrapper<Classroom> classroomQuery = new QueryWrapper<>();
            classroomQuery.eq("classroom_id", classroomId)
                    .eq("date", reserveDateStr)
                    .eq("status", 1); // 可用状态

            List<Classroom> classroomList = classroomMapper.selectList(classroomQuery);
            if (classroomList.isEmpty()) {
                return Result.error("-1", "该教室在所选日期无可用记录");
            }

            Classroom classroom = null;
            if (classroomList.size() > 1) {
                log.warn("教室重复记录：{} {}，共{}条", classroomId, reserveDateStr, classroomList.size());
                List<Classroom> availableList = classroomList.stream()
                        .filter(c -> c.getStatus() == 1)
                        .collect(Collectors.toList());
                if (availableList.isEmpty()) {
                    return Result.error("-1", "该教室在所选日期不可用");
                }
                classroom = availableList.get(0);
            } else {
                classroom = classroomList.get(0);
            }

            // 6. 校验时段冲突
            QueryWrapper<ClassroomReservation> conflictQuery = new QueryWrapper<>();
            conflictQuery.eq("classroom_id", classroomId)
                    .eq("reserve_date", reserveDate)
                    .and(qw -> qw.lt("start_time", endTimeStr)
                            .gt("end_time", startTimeStr))
                    .in("status", 0, 1);

            if (baseMapper.selectCount(conflictQuery) > 0) {
                return Result.error("-1", "该时间段已被预约，请选择其他时间");
            }

            // 7. 保存预约记录
            reservation.setStatus(0); // 0-待审核
            reservation.setCreateTime(LocalDateTime.now());
            save(reservation);

            return Result.success("预约申请已提交，请等待审核");

        } catch (ParseException e) {
            log.error("时间格式错误", e);
            return Result.error("-1", "时间格式错误");
        } catch (Exception e) {
            log.error("提交预约失败", e);
            return Result.error("-1", "提交失败：" + e.getMessage());
        }
    }

    @Override
    public Result<?> auditReservation(Integer id, Integer status, String adminRemark) {
        // 修正：允许 status 为 1（通过）或 2（拒绝）
        if (id == null || status == null || (status != 1 && status != 2)) {
            return Result.error("-1", "参数错误：状态只能是1（通过）或2（拒绝）");
        }

        // 强制校验审批意见
        if (adminRemark == null || adminRemark.trim().isEmpty()) {
            return Result.error("-1", "审批意见不能为空");
        }

        ClassroomReservation reservation = getById(id);
        if (reservation == null) {
            return Result.error("-1", "预约记录不存在");
        }

        // 防止重复审核
        if (reservation.getStatus() != 0) {
            return Result.error("-1", "该预约已审核，不可重复操作");
        }

        // 从Session获取当前审核人信息
        String currentAuditor = getCurrentAuditor();

        // 设置审批信息
        reservation.setStatus(status);
        reservation.setAuditor(currentAuditor);
        reservation.setAuditTime(LocalDateTime.now());
        reservation.setAdminRemark(adminRemark.trim()); // 保存审核意见

        updateById(reservation);
        return Result.success("审核完成：" + (status == 1 ? "已通过" : "已拒绝"));
    }

    // 新增获取当前审核人方法
    private String getCurrentAuditor() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "system";
        }
        HttpSession session = attributes.getRequest().getSession();

        // 从session获取当前登录用户（与项目登录逻辑一致）
        Object userObj = session.getAttribute("User");
        if (userObj instanceof Admin) {
            return ((Admin) userObj).getUsername();
        } else if (userObj instanceof Manager) {
            return ((Manager) userObj).getUsername();
        } else {
            return "unknown";
        }
    }

    @Override
    public Result<?> getUserReservations(String username) {
        QueryWrapper<ClassroomReservation> query = new QueryWrapper<>();
        query.eq("username", username)
                .orderByDesc("create_time");
        return Result.success(list(query));
    }

    @Override
    public Result<?> cancelReservation(Integer id, String username) {
        ClassroomReservation reservation = getById(id);
        if (reservation == null) {
            return Result.error("-1", "预约记录不存在");
        }

        if (!reservation.getUsername().equals(username)) {
            return Result.error("-1", "无权取消他人预约");
        }

//        if (reservation.getStatus() == 1) {
//            return Result.error("-1", "已通过的预约无法取消");
//        }

        removeById(id);
        return Result.success("取消预约成功");
    }

    @Override
    public Result<?> countRecentReservations(String username) {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);

        QueryWrapper<ClassroomReservation> query = new QueryWrapper<>();
        query.eq("username", username)
                .ge("create_time", weekAgo)
                .in("status", 0, 1);

        long count = baseMapper.selectCount(query);
        return Result.success(count);
    }

    @Override
    public Result<?> getPendingReservations(Integer pageNum, Integer pageSize, String identity) {
        Page<ClassroomReservation> page = new Page<>(pageNum, pageSize);

        QueryWrapper<ClassroomReservation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 0)  // 0-待审核
                .orderByAsc("create_time");  // 按创建时间升序

        // 添加身份筛选条件
        if (identity != null && !identity.isEmpty()) {
            queryWrapper.eq("identity", identity);
        }

        Page<ClassroomReservation> resultPage = page(page, queryWrapper);
        return Result.success(resultPage);
    }

    @Override
    public List<ClassroomReservation> listExpiredPendingReservations() {
        LocalDateTime now = LocalDateTime.now();

        QueryWrapper<ClassroomReservation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 0)  // 只处理待审核的
                .and(qw -> qw.lt("reserve_date", now.toLocalDate())  // 日期已过
                        .or(qw2 -> qw2.eq("reserve_date", now.toLocalDate())  // 当天但时间已过
                                .lt("start_time", now.toLocalTime().toString())));

        return baseMapper.selectList(queryWrapper);
    }
    @Override
    public Page<ClassroomReservation> queryReservationList(
            Page<ClassroomReservation> page,
            String identity,
            Integer status,
            String buildingId,
            String reserveDate,
            String keyword) {

        QueryWrapper<ClassroomReservation> queryWrapper = new QueryWrapper<>();

        // 身份筛选
        if (identity != null && !identity.isEmpty()) {
            queryWrapper.eq("identity", identity);
        }
        // 状态筛选
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        // 教学楼筛选
        if (buildingId != null && !buildingId.isEmpty()) {
            queryWrapper.eq("building_id", buildingId);
        }
        // 预约日期筛选
        if (reserveDate != null && !reserveDate.isEmpty()) {
            queryWrapper.eq("reserve_date", reserveDate);
        }
        // 搜索关键词（匹配申请人姓名或学号）
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(qw -> qw
                    .like("reserver_name", keyword)  // 申请人姓名
                    .or().like("username", keyword)); // 学号/工号
        }

        // 按申请时间倒序排序（最新的在前面）
        queryWrapper.orderByDesc("create_time");

        return baseMapper.selectPage(page, queryWrapper);
    }
    @Override
    public Page<ClassroomReservation> getAuditedReservations(
            Page<ClassroomReservation> page,
            String username,
            Integer checkStatus,
            String buildingId,
            String reserveDate,
            String keyword) {

        QueryWrapper<ClassroomReservation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1); // 只查已审核通过的

        // 教学楼筛选
        if (buildingId != null && !buildingId.isEmpty()) {
            queryWrapper.eq("building_id", buildingId);
        }
        // 预约日期筛选
        if (reserveDate != null && !reserveDate.isEmpty()) {
            queryWrapper.eq("reserve_date", reserveDate);
        }
        // 用户名筛选
        if (username != null && !username.isEmpty()) {
            queryWrapper.eq("username", username);
        }
        // 签到状态筛选
        if (checkStatus != null) {
            queryWrapper.eq("check_status", checkStatus);
        }
        // 搜索功能（支持姓名、学号/工号）
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(qw -> qw
                    .like("reserver_name", keyword) // 匹配姓名
                    .or().like("username", keyword)); // 匹配学号/工号
        }

        // 按预约日期倒序排序
        queryWrapper.orderByDesc("reserve_date");
        return baseMapper.selectPage(page, queryWrapper);
    }

    // 签到
    public Result<?> checkIn(Integer reservationId, String username) {
        ClassroomReservation reservation = getById(reservationId);
        if (reservation == null) {
            return Result.error("-1", "预约记录不存在");
        }
        // 权限校验
        if (!reservation.getUsername().equals(username)) {
            return Result.error("-1", "无权操作他人预约");
        }
        // 状态校验（仅已通过的预约可签到）
        if (reservation.getStatus() != 1) {
            return Result.error("-1", "仅已通过的预约可签到");
        }
        // 重复签到校验
        if (reservation.getCheckStatus() != null && reservation.getCheckStatus() >= 1) {
            return Result.error("-1", "已完成签到，不可重复操作");
        }
        // 更新签到信息
        reservation.setCheckinTime(LocalDateTime.now());
        reservation.setCheckStatus(1); // 1-已签到
        updateById(reservation);
        return Result.success("签到成功");
    }

    // 签退
    public Result<?> checkOut(Integer reservationId, String username) {
        ClassroomReservation reservation = getById(reservationId);
        if (reservation == null) {
            return Result.error("-1", "预约记录不存在");
        }
        // 权限校验
        if (!reservation.getUsername().equals(username)) {
            return Result.error("-1", "无权操作他人预约");
        }
        // 状态校验（仅已签到的预约可签退）
        if (reservation.getCheckStatus() == null || reservation.getCheckStatus() != 1) {
            return Result.error("-1", "请先完成签到");
        }
        // 重复签退校验
        if (reservation.getCheckStatus() == 2) {
            return Result.error("-1", "已完成签退，不可重复操作");
        }
        // 更新签退信息
        reservation.setCheckoutTime(LocalDateTime.now());
        reservation.setCheckStatus(2); // 2-已签退
        updateById(reservation);
        return Result.success("签退成功");
    }
    @Override
    public Page<ClassroomReservation> getCheckRecords(
            Page<ClassroomReservation> page,
            String username,
            String identity) {

        QueryWrapper<ClassroomReservation> queryWrapper = new QueryWrapper<>();
        // 只查询已审核的记录（status=1）
        queryWrapper.eq("status", 1);
        // 筛选用户名
        if (username != null && !username.isEmpty()) {
            queryWrapper.eq("username", username);
        }
        // 筛选身份
        if (identity != null && !identity.isEmpty()) {
            queryWrapper.eq("identity", identity);
        }
        // 按预约日期倒序
        queryWrapper.orderByDesc("reserve_date", "start_time");

        return baseMapper.selectPage(page, queryWrapper);
    }

    // 文件：Dormitory_backend/src/main/java/com/example/springboot/service/impl/ClassroomReservationServiceImpl.java
    // 文件：ClassroomReservationServiceImpl.java
    @Override
    public Page<ClassroomReservation> getCheckRecordsByBuilding(
            Page<ClassroomReservation> page,
            String username,
            String identity,
            String buildingId,
            String date) { // 接收日期参数

        QueryWrapper<ClassroomReservation> queryWrapper = new QueryWrapper<>();
        // 仅查询已审核通过的记录（status=1）
        queryWrapper.eq("status", 1);

        // 按用户名筛选
        if (username != null && !username.isEmpty()) {
            queryWrapper.eq("username", username);
        }
        // 按用户身份筛选（stu/teacher）
        if (identity != null && !identity.isEmpty()) {
            queryWrapper.eq("identity", identity);
        }
        // 按楼宇筛选
        if (buildingId != null && !buildingId.isEmpty()) {
            queryWrapper.eq("building_id", buildingId);
        }
        // 按日期筛选（核心：添加预约日期条件）
        if (date != null && !date.isEmpty()) {
            queryWrapper.eq("reserve_date", date); // 匹配预约日期（reserve_date字段）
        }

        // 按预约日期和时间倒序排序
        queryWrapper.orderByDesc("reserve_date", "start_time");
        return baseMapper.selectPage(page, queryWrapper);
    }
}