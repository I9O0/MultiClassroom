package com.example.springboot.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springboot.common.Result;
import com.example.springboot.entity.MultimediaRepair;
import com.example.springboot.entity.Student;
import com.example.springboot.service.MultimediaRepairService;
import com.example.springboot.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/multimedia/repair")
public class MultimediaRepairController {

    @Autowired
    private MultimediaRepairService multimediaRepairService;

    @Autowired
    private StudentService studentService;

    // 提交报修申请
    @PostMapping("/submit")
    public Result submitRepair(@RequestBody MultimediaRepair repair) {
        // 校验学生是否存在
        Student student = studentService.getById(repair.getUsername());
        if (student == null) {
            return Result.error("-1", "不存在该生，请检查学号");
        }

        // 补充默认信息
        repair.setStudentName(student.getName());
        repair.setStatus("pending"); // 初始状态：待处理
        repair.setCreateTime(new Date());

        boolean saved = multimediaRepairService.save(repair);
        return saved ? Result.success("报修提交成功") : Result.error("-1", "提交失败");
    }

    // 查询个人报修记录
    @GetMapping("/myRecords")
    public Result getMyRecords(@RequestParam String username) {
        QueryWrapper<MultimediaRepair> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username)
                .orderByDesc("create_time");
        List<MultimediaRepair> records = multimediaRepairService.list(wrapper);
        return Result.success(records);
    }
}