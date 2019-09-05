package com.guigu.gmall.usermanage.controller;

import com.guigu.gmall.bean.UserInfo;
import com.guigu.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {
    //查询
    @Autowired
    UserService userService;
    @GetMapping("allUser")
    public List<UserInfo>getAllUser(){
        return userService.getUserInfoListAll();
    }
    //添加
    @PostMapping("addUser")
    public String addUser(UserInfo userInfo){
    userService.addUser(userInfo);
    return "成功";
    }
    //修改
    @PostMapping("update")
    public String update(UserInfo userInfo){
        userService.updateUser(userInfo);
        return "seccess";
    }
    //根据名称修改
    @PostMapping("updateUserByName")
    public String updateUserByName(UserInfo userInfo){
        userService.updateUserByName(userInfo.getName(),userInfo);
        return "seccess";
    }

    //删除
    @PostMapping("delete")
    public String delete(UserInfo userInfo){
        userService.delUser(userInfo);
        return "seccess";
    }
    //查某一个用户
    @GetMapping("getUser")
    public UserInfo getUserInfoById(String id){
       return userService.getUseInfoById(id);
    }
}
