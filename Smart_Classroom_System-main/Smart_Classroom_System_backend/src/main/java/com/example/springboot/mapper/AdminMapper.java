package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.Admin;
import org.apache.ibatis.annotations.Param;


public interface AdminMapper extends BaseMapper<Admin> {
    // 仅更新头像
    int updateAvatar(@Param("username") String username, @Param("avatar") String avatar);
}
