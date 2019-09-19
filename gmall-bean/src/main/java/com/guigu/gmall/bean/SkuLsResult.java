package com.guigu.gmall.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
// 全文检索 结果返回
public class SkuLsResult implements Serializable {
    //所有商品对象
    List<SkuLsInfo> skuLsInfoList;

    long total; //条数

    long totalPages; //页数
    //平台属性值id集合
    List<String> attrValueIdList;
}
