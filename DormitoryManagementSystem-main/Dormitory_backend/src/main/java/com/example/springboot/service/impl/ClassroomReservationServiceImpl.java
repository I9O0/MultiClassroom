package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Classroom;
import com.example.springboot.entity.ClassroomReservation;
import com.example.springboot.mapper.ClassroomMapper;
import com.example.springboot.mapper.ClassroomReservationMapper;
import com.example.springboot.service.ClassroomReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClassroomReservationServiceImpl extends ServiceImpl<ClassroomReservationMapper, ClassroomReservation> implements ClassroomReservationService {

    private static final Logger log = LoggerFactory.getLogger(ClassroomReservationServiceImpl.class);
    private static final SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm");

    @Autowired
    private ClassroomMapper classroomMapper;

    @Override
    public Result<?> submitReservation(ClassroomReservation reservation) {
        try {
            // 1. 基础参数校验
            if (reservation.getStudentName() == null || reservation.getStudentName().trim().isEmpty()) {
                return Result.error("-1", "预约人姓名不能为空");
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

            // 3. 一周内预约次数限制
            String studentUsername = reservation.getUsername();
            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);

            QueryWrapper<ClassroomReservation> countQuery = new QueryWrapper<>();
            countQuery.eq("username", studentUsername)
                    .ge("create_time", weekAgo)
                    .in("status", 0, 1);

            long recentCount = baseMapper.selectCount(countQuery);
            if (recentCount >= 1) {
                return Result.error("-1", "一周内只能预约1次，请下周再尝试");
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
        if (id == null || status == null || (status != 0 && status != 1)) {
            return Result.error("-1", "参数错误：状态只能是0或1");
        }

        ClassroomReservation reservation = getById(id);
        if (reservation == null) {
            return Result.error("-1", "预约记录不存在");
        }

        // 防止重复审核
        if (reservation.getStatus() != 0) {
            return Result.error("-1", "该预约已审核，不可重复操作");
        }

        reservation.setStatus(status);
        updateById(reservation);
        return Result.success("审核完成：" + (status == 1 ? "已通过" : "已拒绝"));
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

        if (reservation.getStatus() == 1) {
            return Result.error("-1", "已通过的预约无法取消");
        }

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
    public Result<?> getPendingReservations(Integer pageNum, Integer pageSize) {
        Page<ClassroomReservation> page = new Page<>(pageNum, pageSize);
        QueryWrapper<ClassroomReservation> query = new QueryWrapper<>();
        query.eq("status", 0) // 0=待审核状态
                .orderByAsc("create_time"); // 按申请时间升序

        Page<ClassroomReservation> resultPage = baseMapper.selectPage(page, query);
        return Result.success(resultPage);
    }

}