package com.guigu.gmall.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
//每一件商品
public class OrderDetail implements Serializable {
    @Id
    @Column
    private String id;
    @Column
    private String orderId;
    @Column
    private String skuId;
    @Column
    private String skuName;
    @Column
    private String imgUrl;
    @Column
    private BigDecimal orderPrice;
    @Column
    private Integer skuNum;

    //判断当前是否还有库存 1表示有足够的库存 0表示库存不足
    @Transient
    private String hasStock;
}
