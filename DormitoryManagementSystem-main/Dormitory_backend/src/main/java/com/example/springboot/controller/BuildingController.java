package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Building;
import com.example.springboot.service.BuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 教学楼接口：提供教学楼列表查询（供前端选择）
 */
@RestController
@RequestMapping("/building")
public class BuildingController {

    @Autowired
    private BuildingService buildingService;

    /**
     * 获取所有教学楼列表（用于前端下拉选择）
     */
    @GetMapping("/listAll")
    public Result getAllBuildings() {
        List<Building> buildingList = buildingService.list(); // MyBatis-Plus自带的查询所有方法
        return Result.success(buildingList);
    }
}