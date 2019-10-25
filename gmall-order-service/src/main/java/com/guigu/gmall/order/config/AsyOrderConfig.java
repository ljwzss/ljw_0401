package com.guigu.gmall.order.config;

import com.guigu.gmall.bean.OrderInfo;
import com.guigu.gmall.bean.enums.ProcessStatus;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync //开启异步
public class AsyOrderConfig implements AsyncConfigurer {
    //Executor执行者
    @Override
    public Executor getAsyncExecutor() {
        //定义多线程
        // 获取线程池 – 数据库的连接池
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        // 设置线程数
        threadPoolTaskExecutor.setCorePoolSize(10);
        // 设置最大连接数
        threadPoolTaskExecutor.setMaxPoolSize(100);
        // 设置等待队列，如果10个不够，可以有100个线程等待 缓冲池
        threadPoolTaskExecutor.setQueueCapacity(100);
        // 初始化操作
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;

    }

    //发生异常 处理异常
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        //自定义异常
       // throw  new MyException("错误 咋整啊");
        return null;
    }
    //集成 自定义异常
    class MyException extends Error{
        String msg;
        //构造器
        MyException(String message){
            this.msg=message;
        }
        public void getMsg(){
            //获取父类异常信息
            super.getMessage();
        }
    }

//    // 处理未完成订单
//    @Async
//    public  void execExpiredOrder(OrderInfo orderInfo){
//        // 订单信息
//        updateOrderStatus(orderInfo.getId(), ProcessStatus.CLOSED);
//        // 付款信息
//        paymentService.closePayment(orderInfo.getId());
//    }
}















