package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("multimedia_repair")
public class MultimediaRepair {
    @TableId(type = IdType.AUTO)
    private Integer id; // 报修单ID
    private String username; // 学生学号
    private String studentName; // 学生姓名
    private String buildingId; // 教学楼ID
    private String classroomId; // 教室号
    private String deviceType; // 设备类型
    private String faultDesc; // 故障描述
    private String faultPhoto; // 故障照片路径
    private String status; // 状态：pending-待处理，repairing-维修中，solved-已解决
    private Date createTime; // 提交时间
    private Date handleTime; // 处理时间
    private String handler; // 处理人
    private String handleNote; // 处理备注
}