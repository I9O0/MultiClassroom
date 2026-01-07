package com.example.springboot.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Manager;
import com.example.springboot.mapper.managerMapper;
import com.example.springboot.service.ManagerService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@Service
public class ManagerServiceImpl extends ServiceImpl<managerMapper, Manager> implements ManagerService {

    /**
     * 注入DAO层对象
     */
    @Resource
    private managerMapper managerMapper;

    /**
     * 管理员登录
     */
    @Override
    public Manager managerLogin(String username, String password) {
        QueryWrapper<Manager> qw = new QueryWrapper<>();
        qw.eq("username", username);
        qw.eq("password", password);
        Manager manager = managerMapper.selectOne(qw);
        if (manager != null) {
            return manager;
        } else {
            return null;
        }
    }

    /**
     * 管理员新增
     */
    @Override
    public int addNewmanager(Manager manager) {
        int insert = managerMapper.insert(manager);
        return insert;
    }

    /**
     * 管理员查找
     */
    @Override
    public Page find(Integer pageNum, Integer pageSize, String search) {
        Page page = new Page<>(pageNum, pageSize);
        QueryWrapper<Manager> qw = new QueryWrapper<>();
        qw.like("name", search);
        Page managerPage = managerMapper.selectPage(page, qw);
        return managerPage;
    }

    /**
     * 管理员信息更新
     */
    @Override
    public int updateNewmanager(Manager manager) {
        int i = managerMapper.updateById(manager);
        return i;
    }

    /**
     * 管理员删除
     */
    @Override
    public int deletemanager(String username) {
        int i = managerMapper.deleteById(username);
        return i;
    }


}
