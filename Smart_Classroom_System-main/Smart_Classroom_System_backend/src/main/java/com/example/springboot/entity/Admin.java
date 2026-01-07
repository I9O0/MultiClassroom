package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 后台管理员
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "admin")
public class Admin {

    @TableId(value = "username")
    private String username;
    @TableField(value = "password")
    private String password;
    @TableField(value = "name")
    private String name;
    @TableField(value = "gender")
    private String gender;
    @TableField(value = "age")
    private int age;
    @TableField(value = "phone_num")
    private String phoneNum;
    @TableField(value = "email")
    private String email;
    @TableField("status")
    private int status;

    // 关键新增：密码修改专用临时字段（不映射数据库表）
    @TableField(exist = false) // 告诉MyBatis-Plus该字段不在数据库中
    private String oldPass; // 接收前端传入的原密码
    @TableField(exist = false)
    private String newPass; // 接收前端传入的新密码
}