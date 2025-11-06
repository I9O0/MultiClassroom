package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Building;
import com.example.springboot.mapper.BuildingMapper;
import com.example.springboot.service.BuildingService;
import org.springframework.stereotype.Service;

@Service
public class BuildingServiceImpl extends ServiceImpl<BuildingMapper, Building> implements BuildingService {
    // 无需额外实现，直接使用父类方法
}