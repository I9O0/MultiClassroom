package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.common.Result;
import com.example.springboot.entity.ClassroomReservation;

import java.util.List;

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
    // 修改接口定义，添加identity参数
    Result<?> getPendingReservations(Integer pageNum, Integer pageSize, String identity);

    /**
     * 查询学生近7天内的预约次数（前端提前提示用）
     * @param username 学生用户名
     */
    Result<?> countRecentReservations(String username);


    // 在ClassroomReservationService.java中添加方法
    List<ClassroomReservation> listExpiredPendingReservations();

    // 修改queryReservationList方法定义，添加新参数
    Page<ClassroomReservation> queryReservationList(
            Page<ClassroomReservation> page,
            String identity,       // 身份筛选
            Integer status,        // 状态筛选
            String buildingId,     // 教学楼筛选
            String reserveDate,    // 预约日期筛选
            String keyword);       // 搜索关键词

    /**
     * 查询已审核（status=1）的预约列表
     * @param page 分页对象
     * @param username 可选：用户名，用于筛选个人预约
     * @param checkStatus 可选：签到状态，0-未签到，1-已签到，2-已签退
     * @return 分页结果
     */
    // 在ClassroomReservationService.java中更新方法定义
    Page<ClassroomReservation> getAuditedReservations(
            Page<ClassroomReservation> page,
            String username,
            Integer checkStatus,
            String buildingId,
            String reserveDate,  // 新增日期参数
            String keyword);     // 新增搜索关键词 // 新增参数

    /**
     * 签到操作
     * @param reservationId 预约ID
     * @param username 操作人用户名
     * @return 结果
     */
    Result<?> checkIn(Integer reservationId, String username);

    /**
     * 签退操作
     * @param reservationId 预约ID
     * @param username 操作人用户名
     * @return 结果
     */
    Result<?> checkOut(Integer reservationId, String username);


    /**
     * 管理员查询所有签到签退记录
     */
    Page<ClassroomReservation> getCheckRecords(
            Page<ClassroomReservation> page,
            String username,
            String identity);

    /**
     * 宿管查询本楼宇签到签退记录
     */
    // 文件：ClassroomReservationService.java
    /**
     * 管理员和宿管查询签到签退记录（支持日期筛选）
     */
    Page<ClassroomReservation> getCheckRecordsByBuilding(
            Page<ClassroomReservation> page,
            String username,
            String identity,
            String buildingId,
            String date); // 新增日期参数

}