package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.common.Result;
import com.example.springboot.entity.ClassroomReservation;

public interface ClassroomReservationService extends IService<ClassroomReservation> {
    /**
     * 学生提交预约申请
     */
    Result<?> submitReservation(ClassroomReservation reservation);

    /**
     * 管理员审核预约申请
     * @param id 预约记录ID
     * @param status 审核状态（0=拒绝，1=通过）
     * @param adminRemark 审核备注（可选）
     */
    Result<?> auditReservation(Integer id, Integer status, String adminRemark);

    /**
     * 学生查询自己的预约记录
     * @param username 学生用户名
     */
    Result<?> getUserReservations(String username);

    /**
     * 学生取消预约
     * @param id 预约记录ID
     * @param username 学生用户名（权限校验）
     */
    Result<?> cancelReservation(Integer id, String username);

    /**
     * 管理员查询待审批预约（按申请时间升序排序）
     * @param pageNum 页码
     * @param pageSize 每页条数
     */
    Result<?> getPendingReservations(Integer pageNum, Integer pageSize);

    /**
     * 查询学生近7天内的预约次数（前端提前提示用）
     * @param username 学生用户名
     */
    Result<?> countRecentReservations(String username);


}