package com.guigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.guigu.gmall.bean.*;
import com.guigu.gmall.service.ManageService;

import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@CrossOrigin
public class ManageController {

    @Reference
   ManageService manageService;

    @PostMapping("getCatalog1")
    public List<BaseCatalog1>getBaseCatalog1(){
        List<BaseCatalog1> catalog1 = manageService.getCatalog1();
        return catalog1;
    }
    @PostMapping("getCatalog2")
    public List<BaseCatalog2>getBaseCatalog2Id(String catalog1Id){
        List<BaseCatalog2> catalog2 = manageService.getCatalog2(catalog1Id);
        return catalog2;
    }
    @PostMapping("getCatalog3")
    public List<BaseCatalog3>getBaseCatalog3Id(String catalog2Id){
        List<BaseCatalog3> catalog3 = manageService.getCatalog3(catalog2Id);
        return catalog3;
    }
    //3级查询完显示你的属性
    @GetMapping("attrInfoList")
    public List<BaseAttrInfo> attrInfoList(String catalog3Id){
        List<BaseAttrInfo> attrList = manageService.getAttrList(catalog3Id);
        return attrList ;
    }

    //保存
    @PostMapping ("saveAttrInfo")
    public void saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        // 调用服务层做保存方法
        manageService.saveAttrInfo(baseAttrInfo);
    }
    //保存好 后进行修改
    @PostMapping("getAttrValueList")
    public List<BaseAttrValue>getAttrValueList(String attrId){
       //查询属性
        BaseAttrInfo attrInfo = manageService.getAttrInfo(attrId);
        //单独把 属性值 提取出来
        List<BaseAttrValue> list = attrInfo.getAttrValueList();
        return list;
    }


    // 商品属性 全部添加
    @PostMapping("saveSpuInfo")
    public String saveSpuInfo(@RequestBody SpuInfo spuInfo){
        manageService.saveSpuInfo(spuInfo);
        return "success";
    }
    // 点击添加 后 加载销售属性
    @PostMapping("baseSaleAttrList")
    public List<BaseSaleAttr> getBaseSaleAttrList(){
        return  manageService.getBaseSaleAttrList();
    }

    //3级列表出来以后 属性的显示
    @GetMapping("spuList")
    public List<SpuInfo> getSpuList(String catalog3Id){
        List<SpuInfo> spuList = manageService.getSpuList(catalog3Id);
        return spuList;
    }

    // 根据spuId获取spuImage中的所有图片列表
    @GetMapping("spuImageList")
    public List<SpuImage>spuImageList(String spuId){
        return manageService.getSpuImageList(spuId);
    }

    // 销售属性
    @GetMapping("spuSaleAttrList")
    public List<SpuSaleAttr>getSpuSaleAttrList(String spuId){

        return manageService.getSpuSaleAttrList(spuId);
    }
}


























