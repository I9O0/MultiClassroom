package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.Manager;
import org.apache.ibatis.annotations.Param;


public interface managerMapper extends BaseMapper<Manager> {
    int updateAvatar(@Param("username") String username, @Param("avatar") String avatar);
}
