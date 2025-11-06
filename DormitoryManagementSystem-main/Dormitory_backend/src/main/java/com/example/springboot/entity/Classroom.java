package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("classroom")
public class Classroom implements Serializable {
    private Integer id;
    private String classroomId;
    private String buildingId;
    private String classroomName;
    private Integer seatCount;
    private String multimediaType;
    private Integer status;
    private String description;
    private Date createTime;
    // 新增：与数据库 date 字段对应（驼峰无需处理，直接用 date）
    private String date;
}