package com.example.springboot.unit;

public class ClassroomPermissionUtil {
    /**
     * 校验楼宇访问权限（查询时使用）
     * @param identity 用户身份
     * @param buildingId 教学楼ID
     * @return 无权限返回提示信息，有权限返回null
     */
    public static String checkPermission(String identity, String buildingId) {
        if ("stu".equals(identity)) {
            return ("J1".equals(buildingId) || "J2".equals(buildingId)) ? null : "学生仅可查看J1、J2教学楼";
        } else if ("teacher".equals(identity)) {
            return ("J3".equals(buildingId) || "J4".equals(buildingId)) ? null : "教师仅可查看J3、J4教学楼";
        } else if ("admin".equals(identity) || "manager".equals(identity)) {
            return null; // 超级管理员和管理员（管理员）可查看所有教学楼
        }
        return "身份无效，无查询权限";
    }

    /**
     * 获取默认可见的教学楼（前端下拉框渲染用）
     * @param identity 用户身份
     * @return 教学楼ID逗号分隔字符串
     */
    public static String getDefaultBuildings(String identity) {
        if ("stu".equals(identity)) {
            return "J1,J2";
        } else if ("teacher".equals(identity)) {
            return "J3,J4";
        } else if ("admin".equals(identity) || "manager".equals(identity)) {
            return "J1,J2,J3,J4"; // 超级管理员和管理员可见所有教学楼
        }
        return "";
    }

    /**
     * 获取用户可见的教学楼（与getDefaultBuildings功能一致，供前端渲染下拉框使用）
     * @param identity 用户身份
     * @return 教学楼ID逗号分隔字符串
     */
    public static String getVisibleBuildings(String identity) {
        // 复用getDefaultBuildings的逻辑，避免重复代码
        return getDefaultBuildings(identity);
    }

    /**
     * 校验是否为超级管理员或管理员（修改/删除/新增时使用）
     * @param identity 用户身份
     * @return true=超级管理员/超级管理员，false=非管理员
     */
    public static boolean isAdminOrmanager(String identity) {
        return "admin".equals(identity) || "manager".equals(identity);
    }

    public static boolean canHandleRepair(String identity) {
        return isAdminOrmanager(identity);
    }

    /**
     * 检查是否有权限提交报修
     */
    public static boolean canSubmitRepair(String identity) {
        return "stu".equals(identity) || "teacher".equals(identity);
    }
}