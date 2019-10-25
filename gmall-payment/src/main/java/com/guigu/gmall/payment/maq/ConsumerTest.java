package com.guigu.gmall.payment.maq;

import com.guigu.gmall.config.ActiveMQUtil;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.*;

//消费者
//不在容器中
@Component
public class ConsumerTest {
//    @Autowired
//    static ActiveMQUtil activeMQUtil;

    public static void main(String[] args) throws JMSException {

         /*
       1.创建消息队列工厂
       2.获取连接
       3.打开连接
       4.创建session
       5.创建队列
       6.创建消费者者
       7.接收消息
         */

        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(ActiveMQConnectionFactory.DEFAULT_USER,
                ActiveMQConnectionFactory.DEFAULT_PASSWORD, "tcp://192.168.152.134:61616");

        Connection connection = activeMQConnectionFactory.createConnection();
        connection.start();

        //测试工具类
//        Connection connection = activeMQUtil.getConnection();
//        connection.start();

        //4.创建session
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);//自动取签收处理
        //Session session = connection.createSession(true, Session.SESSION_TRANSACTED); //这是开启事务。底下必须有提交
        //5.创建队列
        Queue queue = session.createQueue("guigu");
        //6.创建提供者
        MessageConsumer consumer = session.createConsumer(queue);

        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                if(message instanceof  TextMessage){
                    try {
                      String  text = ((TextMessage) message).getText();
                        System.out.println("接收的消息为------！"+text);
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
 }