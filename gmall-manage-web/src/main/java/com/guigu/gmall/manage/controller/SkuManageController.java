package com.guigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.guigu.gmall.bean.SkuInfo;
import com.guigu.gmall.service.ManageService;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Transient;

@RestController
@CrossOrigin
public class SkuManageController {

    @Reference
    ManageService manageService;

    @PostMapping("saveSkuInfo")
    public String saveSkuInfo(@RequestBody SkuInfo skuInfo){
        manageService.saveSpuInfo(skuInfo);
        return "success";
    }
}
