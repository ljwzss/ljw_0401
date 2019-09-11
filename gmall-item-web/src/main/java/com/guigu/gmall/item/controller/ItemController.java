package com.guigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.guigu.gmall.bean.SkuImage;
import com.guigu.gmall.bean.SkuInfo;
import com.guigu.gmall.bean.SpuSaleAttr;
import com.guigu.gmall.service.ManageService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

//商品详情功能开发
@Controller
public class ItemController {

    @Reference
    ManageService manageService;

    @RequestMapping("{skuId}.html")
    public String getItem(@PathVariable(value = "skuId") String skuId, HttpServletRequest request){
        //调用服务层

       SkuInfo skuInfo= manageService.getSkuInfo(skuId);
       //图片列表 底下这两行 是 基础版本也可以实现
//       List<SkuImage>skuImageList=manageService.getSkuImageBySkuId(skuId);
//        //图片列表渲染
//        request.setAttribute("skuImageList",skuImageList);

        //获取 销售属性  结果集 就是 你的详情页面展示出来
       List<SpuSaleAttr>spuSaleAttrList= manageService.getSpuSaleAttrListCheckBySku(skuInfo);
        System.out.println(spuSaleAttrList);
       request.setAttribute("spuSaleAttrList",spuSaleAttrList);

        //获取销售属性值  得到属性组合与skuid的映射关系 ，用于页面根据属性组合进行跳转  展示出来可以跳转
        Map skuValueIdsMap = manageService.getSkuValueIdsMap(skuInfo.getSpuId());
        //转化为string 格式
        String valuesSkuJson = JSON.toJSONString(skuValueIdsMap);
        request.setAttribute("valuesSkuJson",valuesSkuJson);

        //保存skuInfo对象数据
       request.setAttribute("skuInfo",skuInfo);
        return "item";
    }



}
