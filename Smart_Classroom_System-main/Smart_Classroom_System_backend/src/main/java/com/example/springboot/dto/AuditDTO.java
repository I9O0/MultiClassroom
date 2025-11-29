package com.example.springboot.dto;

import lombok.Data;

@Data
public class AuditDTO {
    private Integer id; // 预约ID
    private Integer status; // 1=通过，3=拒绝
    private String adminRemark; // 审批意见（必填）
    private String auditor; // 审批人用户名
    private String userRole; // 审批人角色（admin/manager，用于后端校验）
}