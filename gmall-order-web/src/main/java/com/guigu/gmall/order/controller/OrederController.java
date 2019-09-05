package com.guigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.guigu.gmall.bean.UserInfo;
import com.guigu.gmall.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrederController {
    //使用dubbo

    @Reference
    UserService userService;

    @GetMapping("trade") //消费者
    public UserInfo trade(String userid){
        UserInfo userInfo = userService.getUseInfoById(userid);
        return userInfo;
    }
}
