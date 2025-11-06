package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "new_student") // 学生表（对应老师端“teacher”表）
public class Student {

    // 复刻老师端“工号”逻辑：学号为主键
    @TableId(type = IdType.INPUT)
    private String username; // 学号（对应老师端“username”工号）

    private String password; // 密码（加密存储，与老师端一致）
    private String name;     // 姓名（与老师端一致）
    private String phone;    // 手机号（与老师端一致）
    private Integer status;  // 账号状态（1正常/0禁用，与老师端一致）
    private String avatar;   // 头像（与老师端一致）

    // 学生特有字段（对应老师端“department、title”）
    private String major;    // 专业
    private String grade;    // 年级

    // 临时字段：仅用于接收密码修改参数（完全复刻老师端）
    @TableField(exist = false)
    private String oldPass;  // 原密码（明文）
    @TableField(exist = false)
    private String newPass;  // 新密码（明文）
}