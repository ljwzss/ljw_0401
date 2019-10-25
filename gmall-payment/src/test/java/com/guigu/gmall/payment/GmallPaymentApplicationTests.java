package com.guigu.gmall.payment;

import com.guigu.gmall.config.ActiveMQUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPaymentApplicationTests {

	@Autowired
   static ActiveMQUtil activeMQUtil;
	@Test
	public void contextLoads() {
	}

	@Test
	public void testA() throws JMSException {
		Connection connection = activeMQUtil.getConnection();
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










































