package com.example.springboot.unit;

public class ClassroomPermissionUtil {
    // 校验身份与教学楼权限（返回null表示通过，否则返回错误信息）
    public static String checkPermission(String identity, String buildingId) {
        if ("student".equals(identity)) {
            return ("J1".equals(buildingId) || "J2".equals(buildingId)) ? null : "学生仅可操作J1、J2";
        } else if ("teacher".equals(identity)) {
            return ("J3".equals(buildingId) || "J4".equals(buildingId)) ? null : "老师仅可操作J3、J4";
        }
        return "身份无效";
    }

    // 获取默认教学楼（学生J1,J2；老师J3,J4）
    public static String getDefaultBuildings(String identity) {
        return "student".equals(identity) ? "J1,J2" : "J3,J4";
    }
}