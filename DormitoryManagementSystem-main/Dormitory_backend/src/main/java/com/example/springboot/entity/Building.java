package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("building") // 关联教学楼表
public class Building {
    /**
     * 教学楼唯一标识（主键）
     * 例如："J1"、"实训楼A"
     */
    @TableId
    private String buildingId;

    /**
     * 教学楼名称（如“第一教学楼”）
     */
    private String buildingName;

    /**
     * 教学楼位置（如“学校东门北侧”）
     */
    private String location;

    /**
     * 备注信息
     */
    private String description;

    /**
     * 记录创建时间（自动填充）
     */
    private Date createTime;
}