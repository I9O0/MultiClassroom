package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备类型字典实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("device_type")
public class DeviceType {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String typeName; // 设备类型名称
    private Integer sort; // 排序
}