package com.guigu.gmall.manage.mapper;


import com.guigu.gmall.bean.BaseAttrInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;


public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {

    //根据三级分类id查询属性表
    public List<BaseAttrInfo> getBaseAttrInfoListByCatalog3Id(String catalog3Id);
}
