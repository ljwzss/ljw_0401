package com.guigu.gmall.manage.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.guigu.gmall.bean.SkuInfo;
import com.guigu.gmall.bean.SkuLsInfo;
import com.guigu.gmall.service.ListService;
import com.guigu.gmall.service.ManageService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class SkuManageController {

    @Reference
    ManageService manageService;

    @Reference
    ListService listService;
    //前台数据以Json对象的形式传递到后台，后台将json字符串封装成java对象
    @PostMapping("saveSkuInfo")
    public String saveSkuInfo(@RequestBody SkuInfo skuInfo){
        //防止空指针
        if(skuInfo!=null){
            manageService.saveSpuInfo(skuInfo);
        }
        return "success";
    }


    //全文检索  添加 上架------------------------
    @RequestMapping("onSale")
    public String onSale(String skuId){
        //创建要保存的数据对象
        SkuLsInfo skuLsInfo = new SkuLsInfo();
        //通过skuid获取skuInfo
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        //拷贝属性
        BeanUtils.copyProperties(skuInfo,skuLsInfo);
        listService.saveSkuLsInfo(skuLsInfo);
        return "success";
    }
}

















