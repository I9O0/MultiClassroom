package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.Manager;


public interface ManagerService extends IService<Manager> {

    //学生登陆
    Manager managerLogin(String username, String password);

    //新增学生
    int addNewmanager(Manager manager);

    //查询学生
    Page find(Integer pageNum, Integer pageSize, String search);

    //更新学生信息
    int updateNewmanager(Manager manager);

    //删除学生信息
    int deletemanager(String username);
}
