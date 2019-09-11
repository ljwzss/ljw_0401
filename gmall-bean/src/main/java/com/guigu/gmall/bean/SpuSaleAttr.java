package com.guigu.gmall.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.List;
//销售属性表--
@Data
@NoArgsConstructor
public class SpuSaleAttr  implements Serializable {

    @Id
    @Column
    String id ;

    @Column
    String spuId;

    @Column
    String saleAttrId;

    @Column
    String saleAttrName;

    //销售属性值表
    @Transient
    List<SpuSaleAttrValue> spuSaleAttrValueList;
}
