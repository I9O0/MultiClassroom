package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField; // 新增导入
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "teacher")
public class Teacher {

    @TableId(type = IdType.INPUT)
    private String username;

    private String password;

    private String name;

    private String department;

    private String title;

    private String phone;

    private Integer status;

    // 关键修改：添加 @TableField(exist = false)，告诉MyBatis-Plus这两个字段不存在于数据库
    @TableField(exist = false) // 忽略此字段，不参与SQL生成
    private String oldPass; // 仅用于接收前端原密码，不存数据库

    @TableField(exist = false) // 忽略此字段，不参与SQL生成
    private String newPass; // 仅用于接收前端新密码，不存数据库
}