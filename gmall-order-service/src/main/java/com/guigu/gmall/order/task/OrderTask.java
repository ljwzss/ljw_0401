package com.guigu.gmall.order.task;


import com.alibaba.dubbo.config.annotation.Reference;
import com.guigu.gmall.bean.OrderInfo;
import com.guigu.gmall.service.OrderService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

//springboot整合了自家的spring task
@EnableScheduling //开启定时任务
@Component
public class OrderTask {

    @Reference
    OrderService orderService;
      //分时日月周
     // 5 每分钟的第五秒
    // 0/5 没隔五秒执行一次
//    @Scheduled(cron = "5 * * * * ?")
//    public void  work(){
//        System.out.println("毕业了====没学会 "+ Thread.currentThread());
//    }
//    @Scheduled(cron = "0/5 * * * * ?")
//    public void  work1(){
//        System.out.println("学完了=======走你 "+ Thread.currentThread());
//    }

    @Scheduled(cron = "0/20 * * * * ?")
    public void checkOrder(){
        /*
        查询过期订单
        循环处理过期订单
         */

        System.out.println("开始处理过期订单");
        long starttime = System.currentTimeMillis();
        List<OrderInfo> orderList = orderService.getExpiredOrderList();
        if(orderList!=null && orderList.size()>0) {
            for (OrderInfo orderInfo : orderList) {
                // 处理未完成订单 处理过期的订单方法创建
                orderService.execExpiredOrder(orderInfo);
            }
        }
//        long costtime = System.currentTimeMillis() - starttime;
//        System.out.println("一共处理"+expiredOrderList.size()+"个订单 共消耗"+costtime+"毫秒");
    }
}













