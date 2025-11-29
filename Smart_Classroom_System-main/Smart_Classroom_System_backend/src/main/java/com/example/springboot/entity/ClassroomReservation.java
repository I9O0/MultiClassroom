package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("classroom_reservation")
public class ClassroomReservation {
    @TableId(type = IdType.AUTO)
    private Integer id; // 预约ID
    private String username; // 预约学生/老师学号/工号
    private String reserverName; // 预约人姓名
    private String buildingId; // 教学楼ID
    private String classroomId; // 教室号

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate reserveDate; // 预约日期

    private String startTime; // 开始时间（如"08:00"）
    private String endTime; // 结束时间（如"10:00"）
    private String reserveReason; // 预约理由
    private Integer status; // 状态：0-待审核，1-已通过，2-已过期
    private String identity;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime; // 提交时间
    @TableField("admin_remark")
    private String adminRemark; // 审批意见（通过备注/拒绝理由）
    @TableField("auditor")
    private String auditor;
    @TableField("audit_time")
    private LocalDateTime auditTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime checkinTime; // 签到时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime checkoutTime; // 签退时间

    private Integer checkStatus;

}