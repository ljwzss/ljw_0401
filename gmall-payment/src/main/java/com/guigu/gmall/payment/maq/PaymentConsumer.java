package com.guigu.gmall.payment.maq;

import com.alibaba.dubbo.config.annotation.Reference;
import com.guigu.gmall.bean.PaymentInfo;
import com.guigu.gmall.service.PaymentService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class PaymentConsumer {

    @Reference
    PaymentService paymentService;
    //PAYMENT_RESULT_CHECK_QUEUE 监听消息队列 有消息才会监听
    @JmsListener(destination = "PAYMENT_RESULT_CHECK_QUEUE",containerFactory = "jmsQueueListener")
    public void consumeSkuDeduct(MapMessage mapMessage) throws JMSException {
        //获取消息队列中的数据
        String outTradeNo = mapMessage.getString("outTradeNo");
        int delaySec = mapMessage.getInt("delaySec");
        int checkCount = mapMessage.getInt("checkCount");
        //根据outTradeNo查询paymentInfo
        //select * from paymentInfo where out_trade_no ? outTradeNo
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(outTradeNo);
        PaymentInfo paymentInfoQuery = paymentService.getPaymentInfo(paymentInfo);

        //调用检查是否支付的方法  //需要outTradeNo 和 orderId
        boolean result = paymentService.checkPayment(paymentInfoQuery);
        System.out.println("检查结果"+result);
        //检查支付结果
        if(!result && checkCount>0){
            //继续检查 在发送消息
            System.out.println("检查次数"+checkCount);
            paymentService.sendDelayPaymentResult(outTradeNo,delaySec,checkCount-1);
        }
    }
}



















