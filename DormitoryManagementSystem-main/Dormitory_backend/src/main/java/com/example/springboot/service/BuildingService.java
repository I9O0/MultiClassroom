package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.Building;

public interface BuildingService extends IService<Building> {
    // 直接继承IService，无需额外方法（使用自带的list()查询所有）
}