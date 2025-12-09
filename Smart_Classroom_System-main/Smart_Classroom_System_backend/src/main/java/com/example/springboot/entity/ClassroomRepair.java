package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 教室设备报修单实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("classroom_repair") // 对应数据库表名
public class ClassroomRepair {
    @TableId(type = IdType.AUTO)
    private Integer id; // 主键ID

    private String reporterId; // 报修人ID（学号/工号）
    private String reporterName; // 报修人姓名
    private String reporterRole; // 报修人角色（student/teacher）

    @TableField("building_id")
    private String buildingId; // 教学楼ID（关联Building表）
    @TableField("classroom_id")
    private String classroomId; // 教室ID（关联Classroom表）
    private String deviceType; // 设备类型（如投影仪、音响等）
    private String problemDetail; // 问题描述

    private Integer status; // 报修状态：pending-待处理，repairing-维修中，solved-已解决
    @TableField("submit_time")
    private LocalDateTime submitTime; // 提交时间
    private String faultPhotos;
    private String handlerId; // 处理人ID（维修人员/管理员ID）
    private String handlerName; // 处理人姓名
    @TableField("handle_time")
    private LocalDateTime handleTime; // 处理完成时间
    private String handleResult; // 处理结果描述
}