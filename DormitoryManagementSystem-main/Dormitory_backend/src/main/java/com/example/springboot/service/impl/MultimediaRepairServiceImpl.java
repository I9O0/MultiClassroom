package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.MultimediaRepair;
import com.example.springboot.mapper.MultimediaRepairMapper;
import com.example.springboot.service.MultimediaRepairService;
import org.springframework.stereotype.Service;

// 只给实现类加@Service，注册为唯一的Bean
@Service
public class MultimediaRepairServiceImpl extends ServiceImpl<MultimediaRepairMapper, MultimediaRepair> implements MultimediaRepairService {
}