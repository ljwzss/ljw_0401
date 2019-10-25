package com.guigu.gmall.bean;


import com.guigu.gmall.bean.enums.OrderStatus;
import com.guigu.gmall.bean.enums.PaymentWay;
import com.guigu.gmall.bean.enums.ProcessStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
//提交订单  购物车列表
public class OrderInfo implements Serializable {
    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    //用户
    @Column
    private String consignee;

    @Column
    private String consigneeTel;


    @Column
    private BigDecimal totalAmount;

    @Column
    private OrderStatus orderStatus;
    //订单状态
    @Column
    private ProcessStatus processStatus;


    @Column
    private String userId;

    //支付方式
    @Column
    private PaymentWay paymentWay;
    //过期时间
    @Column
    private Date expireTime;

    //地址
    @Column
    private String deliveryAddress;

    //备注
    @Column
    private String orderComment;

    @Column
    private Date createTime;

    @Column
    private String parentOrderId;

    @Column
    private String trackingNo;


    //送货清单----名字很重要
    @Transient
    private List<OrderDetail> orderDetailList;


    @Transient
    private String wareId;

    @Column
    private String outTradeNo;

    //计算总价格
    public void sumTotalAmount() {
        BigDecimal totalAmount = new BigDecimal("0");
        for (OrderDetail orderDetail : orderDetailList) {
            totalAmount = totalAmount.add(orderDetail.getOrderPrice().multiply(new BigDecimal(orderDetail.getSkuNum())));
        }
        this.totalAmount = totalAmount;
    }
}
