package com.guigu.gmall.service;



import com.guigu.gmall.bean.UserInfo;

import java.util.List;

public interface UserService {

    //查所有
    List<UserInfo> getUserInfoListAll();
    //新增
    void addUser(UserInfo userInfo);
    //修改
    void updateUser(UserInfo userInfo);
    //按照用户名修改
    void updateUserByName(String name, UserInfo userInfo);
    //删除
    void delUser(UserInfo userInfo);
    //查询单个
    UserInfo getUseInfoById(String id);
}
