package com.guigu.gmall.payment.maq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;

//提供者
public class ProducerTest {
    public static void main(String[] args) throws JMSException {
        /*
       1.创建消息队列工厂
       2.获取连接
       3.打开连接
       4.创建session
       5.创建队列
       6.创建提供者
       7.创建消息对象
       8.发送消息
       9.关闭
         */
        //1.创建消息队列工厂
       ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://192.168.152.134:61616");
      //2.获取连接
       Connection connection = connectionFactory.createConnection();
       // 3.打开连接
       connection.start();
       //4.创建session  fale 是否开启事务 后边是事物对应的处理方式
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //Session session = connection.createSession(true, Session.SESSION_TRANSACTED); //这是开启事务。底下必须有提交
        //5.创建队列
        Queue queue = session.createQueue("guigu");
        //6.创建提供者
        MessageProducer producer = session.createProducer(queue);
        //7.创建消息对象
        ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
        //8.发送消息
        activeMQTextMessage.setText(" Hello");
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);//持久化
        producer.send(activeMQTextMessage);
        //9.关闭
        //session.commit();
        producer.close();
        session.close();
        connection.close();
    }
}











