package com.guigu.gmall.service;

import com.guigu.gmall.bean.PaymentInfo;

import java.util.Map;

//支付
public interface PaymentService {
    //保存交易记录
    void  savyPaymentInfo(PaymentInfo paymentInfo);

    //查询交易记录
    PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery);
    //更新交易记录
    void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUpd);

    //退款
    boolean refund(String orderId);

    //微信支付
    Map createNative(String orderId, String totalAmount);

    //支付模块 消息队列  paymentInfo（封装了orderId）
    public void sendPaymentResult(PaymentInfo paymentInfo,String result);

    //延迟加载 根据 out_trade_no来查--查询验证
    boolean checkPayment(PaymentInfo paymentInfoQuery);

    //发送消息查询是否成功
    public void sendDelayPaymentResult(String outTradeNo,int delaySec ,int checkCount);
    //根据orderId关闭交易记录
    void closePayment(String orderId);
}
