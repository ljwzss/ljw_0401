package com.guigu.gmall.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor
@Data
//全文检索
public class SkuLsInfo implements Serializable {

    String id;

    BigDecimal price;

    String skuName;

    String catalog3Id;

    String skuDefaultImg;
    //热度排名
    Long hotScore=0L;

    List<SkuLsAttrValue> skuAttrValueList;
}
