package com.guigu.gmall.service;

import com.guigu.gmall.bean.OrderInfo;
import com.guigu.gmall.bean.enums.ProcessStatus;

import java.util.List;
import java.util.Map;

//提交订单
public interface OrderService {

    //保存订单数据
    public  String  saveOrder(OrderInfo orderInfo);

    // 生成流水号
    public  String getTradeNo(String userId);

    // 验证流水号  对比
    public  boolean checkTradeCode(String userId,String tradeCodeNo);

    //删除流水号
    public void  delTradeCode(String userId);

    //仓库
    boolean checkStock(String skuId, Integer skuNum);

    //支付
    OrderInfo getOrderInfo(String orderId);

    //订单更新
    void updateOrderStatus(String orderId, ProcessStatus processStatus);
    //更新之后减库存
    void sendOrderStatus(String orderId);
    //查询所有过期订单
    List<OrderInfo> getExpiredOrderList();
    //处理过期的订单
    void execExpiredOrder(OrderInfo orderInfo);


    //将orderInfo转为json
    Map initWareOrder(OrderInfo orderInfo);
    //拆单
    List<OrderInfo> orderSplit(String orderId, String wareSkuMap);
}
