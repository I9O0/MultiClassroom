package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.DormManager;
import org.apache.ibatis.annotations.Param;


public interface DormManagerMapper extends BaseMapper<DormManager> {
    int updateAvatar(@Param("username") String username, @Param("avatar") String avatar);
}
