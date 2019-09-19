package com.guigu.gmall.list.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.guigu.gmall.bean.*;
import com.guigu.gmall.service.ListService;
import com.guigu.gmall.service.ManageService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

@Controller
public class ListController {

    @Reference
    ListService listService;
    @Reference
    ManageService manageService;

    //http://list.gmall.com/list.html?catalog3Id=86 首页跳转
    @RequestMapping("list.html")
    public String getSearch(SkuLsParams skuLsParams,HttpServletRequest request){

        //每页显示几条 数据---分页
        skuLsParams.setPageSize(2);

        SkuLsResult skuLsResult = listService.search(skuLsParams);
        String jsonString = JSON.toJSONString(skuLsResult);
        System.out.println(jsonString);
//        return jsonString;
        //获取skuInfo集合 并显示到页面
        List<SkuLsInfo> skuLsInfoList = skuLsResult.getSkuLsInfoList();

        //获取平台属性 平台属性值id-------------------------
        List<BaseAttrInfo>baseAttrInfoList=null;
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();

        /*
        SELECT * FROM base_attr_info bi INNER JOIN base_attr_value bv
        ON bi.id=bv.attr_id
        WHERE bv.id in (81,82,83,120)
         */
        //分开走：三级分类id
//        if(skuLsParams.getCatalog3Id()!=null){
//            baseAttrInfoList=manageService.getAttrList(skuLsParams.getCatalog3Id());
//        }else {
//            //通过平台属性值id查询平台属性，平台属性值 81,82,83,147,148
//            baseAttrInfoList =manageService.getAttrList(attrValueIdList);
//        }
        //都通过平台属性值id 检索
        baseAttrInfoList =manageService.getAttrList(attrValueIdList);

        //编写一个方法记录当前的查询条件-----------------------------点击条件进行跳转
            String urlParam=makeUrlParam(skuLsParams);

        // 声明一个面包屑集合--------- 平台属性名称：平台属性值名称
        ArrayList<BaseAttrValue> baseAttrValueArrayList = new ArrayList<>();

        //集合在遍例的过程中要删除对应的数据 itco
        for (Iterator<BaseAttrInfo> iterator = baseAttrInfoList.iterator(); iterator.hasNext(); ) {
            //获取平台属性对象
            BaseAttrInfo baseAttrInfo =  iterator.next();
            //获取平台属性值集合
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            //获取到集合中的平台属性值id
            for (BaseAttrValue baseAttrValue : attrValueList) {
                //获取URL 上的valueId在skuLsParams对象中
                if(skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){
                    for (String valueId : skuLsParams.getValueId()) {
                        if(valueId.equals(baseAttrValue.getId())){
                            //删除平台属性对象
                            iterator.remove();
                            //构成面包屑  平台属性名称：平台属性值名称
                            BaseAttrValue baseAttrValueed = new BaseAttrValue();
                            baseAttrValueed.setValueName(baseAttrInfo.getAttrName()+":"+baseAttrValue.getValueName());

                            String newUrlParam=makeUrlParam(skuLsParams,valueId);
                            //保存点击面包屑之后的url参数
                            baseAttrValueed.setUrlParam(newUrlParam);

                            //将面包屑添加到集合
                            baseAttrValueArrayList.add(baseAttrValueed);
                        }
                    }
                }
            }
        }


        //保存数据
        request.setAttribute("skuLsInfoList",skuLsInfoList);
        //保存数据到前台页面
        request.setAttribute("baseAttrInfoList",baseAttrInfoList);

       request.setAttribute("urlParam",urlParam);

       //保存面包屑
        request.setAttribute("baseAttrValueArrayList",baseAttrValueArrayList);
        //保存检索关键字
        request.setAttribute("keyword",skuLsParams.getKeyword()  );

        //作用域中将  分页  信息保存 ----------分页信息 es 已经封装了
        request.setAttribute("totalPages",skuLsResult.getTotalPages());
        request.setAttribute("pageNo",skuLsParams.getPageNo());


        return "list";
    }

    //编写记录查询条件的方法
    //excludeValueIds--用户点击的面包屑隐藏的实体类id
    //skuLsParams-----用户查询的参数实体类
    private String makeUrlParam(SkuLsParams skuLsParams,String... excludeValueIds) {
        String urlParam="";
        //判断keyword
        if(skuLsParams.getKeyword()!=null && skuLsParams.getKeyword().length()>0){
            urlParam+="keyword="+skuLsParams.getKeyword();
        }
        //三级分类id
        if(skuLsParams.getCatalog3Id()!=null && skuLsParams.getCatalog3Id().length()>0){
            if(urlParam.length()>0){
                urlParam+="&";
            }
            urlParam+="catalog3Id="+skuLsParams.getCatalog3Id();
        }
        //平台属性id
        if(skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){
            for (String valueId : skuLsParams.getValueId() ) {
                //删除面包屑
                if(excludeValueIds!=null && excludeValueIds.length>0){
                    String excludeValueId = excludeValueIds[0];
                    if(excludeValueId.equals(valueId)){
                        continue;
                    }
                }
                if(urlParam.length()>0){
                    urlParam+="&";
                }
                urlParam+="valueId="+valueId;
            }
        }
        return urlParam;
    }
}















