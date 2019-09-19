package com.guigu.gmall.manage.mapper;


import com.guigu.gmall.bean.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;


public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {

    //根据三级分类id查询属性表
    public List<BaseAttrInfo> getBaseAttrInfoListByCatalog3Id(String catalog3Id);

    //通过平台属性值id查询平台属性，平台属性值 81,82,83,147,148
    List<BaseAttrInfo> selectAttrInfoListByIds(@Param("valueIds") String valueIds);
}
