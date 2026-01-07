package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  管理员
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "manager")
public class Manager {

    @TableId("username")
    private String username;
    @TableField("password")
    private String password;

    @TableField("name")
    private String name;
    @TableField("gender")
    private String gender;
    @TableField("age")
    private int age;
    @TableField("phone_num")
    private String phoneNum;
    @TableField("email")
    private String email;

    // 关键新增：密码修改专用临时字段（不映射数据库）
    @TableField(exist = false) // 告诉MyBatis-Plus该字段不在数据库表中
    private String oldPass; // 接收前端传入的原密码
    @TableField(exist = false)
    private String newPass; // 接收前端传入的新密码
}