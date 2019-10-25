package com.guigu.gmall.order.mq;

import com.alibaba.dubbo.config.annotation.Reference;
import com.guigu.gmall.bean.enums.ProcessStatus;
import com.guigu.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

//消费端  mqconfig中有一个监听器
@Component
public class OrderConsumer {

    @Reference
    OrderService orderService;
    //要监听的对象 网页上可以看见    监听的名称  new MessageListener() =这个注解
    @JmsListener(destination = "PAYMENT_RESULT_QUEUE",containerFactory = "jmsQueueListener")
    public void  consumerPaymentResult(MapMessage mapMessage) throws JMSException {
        //获取消息队列中的数据
        String orderId = mapMessage.getString("orderId");
        String result = mapMessage.getString("result");
        System.out.println("result = " + result);
        System.out.println("orderId = " + orderId);
        //判断支付结果
        if ("success".equals(result)){
        //更新订单信息
        orderService.updateOrderStatus(orderId, ProcessStatus.PAID);
        //更新之后减库存
            orderService.sendOrderStatus(orderId);
            orderService.updateOrderStatus(orderId,ProcessStatus.NOTIFIED_WARE);
        }
    }
    //订单根据库存发送的消息进行更新状态！ 库存里的WareConsumer状态
    @JmsListener(destination = "SKU_DEDUCT_QUEUE",containerFactory = "jmsQueueListener")
    public void consumeSkuDeduct(MapMessage mapMessage) throws JMSException {
        String orderId = mapMessage.getString("orderId");
        String status= mapMessage.getString("status");
        System.out.println("result = " + status);
        System.out.println("orderId = " + orderId);
        if("DEDUCTED".equals(status)){
            //待发货
            orderService.updateOrderStatus(orderId,ProcessStatus.WAITING_DELEVER);
        }
    }

}





































