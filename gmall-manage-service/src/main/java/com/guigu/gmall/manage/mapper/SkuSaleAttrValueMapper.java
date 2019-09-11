package com.guigu.gmall.manage.mapper;

import com.guigu.gmall.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {

    public List<Map>getSaleAttrValuesBySpu(String spuId);
}
