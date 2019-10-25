package com.guigu.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;


import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.github.wxpay.sdk.WXPayUtil;
import com.guigu.gmall.bean.OrderInfo;
import com.guigu.gmall.bean.PaymentInfo;
import com.guigu.gmall.bean.enums.PaymentStatus;
import com.guigu.gmall.config.ActiveMQUtil;
import com.guigu.gmall.payment.mapper.PaymentInfoMapper;
import com.guigu.gmall.service.OrderService;
import com.guigu.gmall.service.PaymentService;
import com.guigu.gmall.util.HttpClient;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.redisson.misc.Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {

    // 服务号Id
    @Value("${appid}")
    private String appid;
    // 商户号Id
    @Value("${partner}")
    private String partner;
    // 密钥
    @Value("${partnerkey}")
    private String partnerkey;


    @Autowired
    PaymentInfoMapper paymentInfoMapper;
    @Reference
    OrderService orderService;
    @Autowired
    AlipayClient alipayClient;
    @Autowired
    ActiveMQUtil activeMQUtil;

    @Override
    public void savyPaymentInfo(PaymentInfo paymentInfo) {

        paymentInfoMapper.insertSelective(paymentInfo);
    }

    //查询交易记录
    @Override
    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery) {

        return paymentInfoMapper.selectOne(paymentInfoQuery);
    }

    //更新交易记录
    @Override
    public void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUpd) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo", out_trade_no);
        paymentInfoMapper.updateByExampleSelective(paymentInfoUpd, example);
    }

    //退款
    @Override
    public boolean refund(String orderId) {

        // AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do","app_id","your private_key","json","GBK","alipay_public_key","RSA2");
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        //根据orderId查询ordeiInfo
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        //设置map封装参数
        HashMap<String, Object> map = new HashMap<>();

        map.put("out_trade_no", orderInfo.getOutTradeNo());
        map.put("refund_amount", orderInfo.getTotalAmount());
        map.put("refund_reason", "退款");
        request.setBizContent(JSON.toJSONString(map));
//        request.setBizContent("{" +
//                "\"out_trade_no\":\"20150320010101001\"," +
//                "\"trade_no\":\"2014112611001004680073956707\"," +
//                "\"refund_amount\":200.12," +
//                "\"refund_currency\":\"USD\"," +
//                "\"refund_reason\":\"正常退款\"," +
//                "\"out_request_no\":\"HZ01RF001\"," +
//                "\"operator_id\":\"OP001\"," +
//                "\"store_id\":\"NJ_S_001\"," +
//                "\"terminal_id\":\"NJ_T_001\"," +
//                "      \"goods_detail\":[{" +
//                "        \"goods_id\":\"apple-01\"," +
//                "\"alipay_goods_id\":\"20010001\"," +
//                "\"goods_name\":\"ipad\"," +
//                "\"quantity\":1," +
//                "\"price\":2000," +
//                "\"goods_category\":\"34543238\"," +
//                "\"categories_tree\":\"124868003|126232002|126252004\"," +
//                "\"body\":\"特价手机\"," +
//                "\"show_url\":\"http://www.alipay.com/xxx.jpg\"" +
//                "        }]," +
//                "      \"refund_royalty_parameters\":[{" +
//                "        \"royalty_type\":\"transfer\"," +
//                "\"trans_out\":\"2088101126765726\"," +
//                "\"trans_out_type\":\"userId\"," +
//                "\"trans_in_type\":\"userId\"," +
//                "\"trans_in\":\"2088101126708402\"," +
//                "\"amount\":0.1," +
//                "\"amount_percentage\":100," +
//                "\"desc\":\"分账给2088101126708402\"" +
//                "        }]," +
//                "\"org_pid\":\"2088101117952222\"" +
//                "  }");
        AlipayTradeRefundResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {

            System.out.println("调用成功");
            return true;
        } else {
            System.out.println("调用失败");
            return false;
        }
    }

    //微信支付
    @Override
    public Map createNative(String orderId, String totalAmount) {
    /*
    根据api文档制作参数
    将参数以xml发送给支付接口
    获取支付结果
     */
        //创建参数
        HashMap<String, String> param = new HashMap<>();//创建参数
        param.put("appid", appid);//公众号
        param.put("mch_id", partner);//商户号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        param.put("body", "尚硅谷");//商品描述
        param.put("out_trade_no", orderId);//商户订单号
        param.put("total_fee", totalAmount);//总金额（分）
        param.put("spbill_create_ip", "127.0.0.1");//IP
        param.put("notify_url", "http://trade.gmall.com/trade");//回调地址(随便写)
        param.put("trade_type", "NATIVE");//交易类型
        //生成要发送 xml

        try {
            //调用工具类 commit中
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println(xmlParam);
            //发送到接口上
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            //设置https
            client.setHttps(true);
            //发送xmlParam
            client.setXmlParam(xmlParam);
            //设置发送方式
            client.post();
            //获得结果
            String result = client.getContent();
            System.out.println(result);
            //传化为map
            Map<String, String> xmlToMap = WXPayUtil.xmlToMap(result);

            HashMap<String, Object> resultMap = new HashMap<>();
            resultMap.put("code_url", xmlToMap.get("code_url"));//支付地址
            resultMap.put("total_fee", totalAmount);//总金额
            resultMap.put("out_trade_no", orderId);//订单号
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            //return new HashMap<>();
        }
        return null;
    }

    //mq
    @Override
    public void sendPaymentResult(PaymentInfo paymentInfo, String result) {
        //创建打开连接
        Connection connection = activeMQUtil.getConnection();
        try {
            connection.start();
            //创建session  开启了事物 下方得commit
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            //创建队列
            Queue queue = session.createQueue("PAYMENT_RESULT_QUEUE");
            //创建消息提供者
            MessageProducer producer = session.createProducer(queue);
            //创建消息对象
            ActiveMQMapMessage activeMQMapMessage = new ActiveMQMapMessage();
            activeMQMapMessage.setString("orderId", paymentInfo.getOrderId());
            activeMQMapMessage.setString("result", result);
            //发送消息
            producer.send(activeMQMapMessage);
            //提交
            session.commit();
            //关闭
            producer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    // 延迟加载 --------查询验证
    @Override
    public boolean checkPayment(PaymentInfo paymentInfoQuery) {

        //AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", "app_id", "your private_key", "json", "GBK", "alipay_public_key", "RSA2");
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        // 声明一个map集合
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no",paymentInfoQuery.getOutTradeNo());
        request.setBizContent(JSON.toJSONString(map));
//        request.setBizContent("{" +
//                "\"out_trade_no\":\"20150320010101001\"," +
//                "\"trade_no\":\"2014112611001004680 073956707\"," +
//                "\"org_pid\":\"2088101117952222\"," +
//                "      \"query_options\":[" +
//                "        \"TRADE_SETTE_INFO\"" +
//                "      ]" +
//                "  }");
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        //在支付宝中是否有该笔交易-----相当于异步回调
        if (response.isSuccess()) {
            System.out.println("调用成功");
            // 判断 交易状态
            if("TRADE_SUCCESS".equals(response.getTradeStatus())||"TRADE_FINISHED".equals(response.getTradeStatus())){
                System.out.println("支付成功");
            //更改交易记录的状态 paymentInfo
                PaymentInfo paymentInfoUpd = new PaymentInfo();
                paymentInfoUpd.setPaymentStatus(PaymentStatus.PAID);
                updatePaymentInfo(paymentInfoQuery.getOutTradeNo(),paymentInfoUpd);
                //paymentInfoQuery必须得有orderId才能正常发送消息队列给订单
                sendPaymentResult(paymentInfoQuery,"success");
                return true;
            }
        } else {
            System.out.println("调用失败");
            return false;
        }
        return false;
    }


    /**
     * 延迟队列反复调用
     * @param outTradeNo 单号
     * @param delaySec 延迟秒
     * @param checkCount 几次
     */
    //生成二维码之后调用该方法，发送一个消息
    @Override
    public void sendDelayPaymentResult(String outTradeNo, int delaySec, int checkCount) {
        //创建打开连接
        Connection connection = activeMQUtil.getConnection();
        try {
            connection.start();
            //创建session  开启了事物 下方得commit
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            //创建队列
            Queue queue = session.createQueue("PAYMENT_RESULT_CHECK_QUEUE");
            //创建消息提供者
            MessageProducer producer = session.createProducer(queue);
            //创建消息对象
            ActiveMQMapMessage activeMQMapMessage = new ActiveMQMapMessage();
            activeMQMapMessage.setString("outTradeNo", outTradeNo);
            activeMQMapMessage.setInt("delaySec",delaySec);
            activeMQMapMessage.setInt("checkCount",checkCount);
            // 设置延迟多少时间
            activeMQMapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,delaySec*1000);
            //发送消息
            producer.send(activeMQMapMessage);
            //提交
            session.commit();
            //关闭
            producer.close();
            session.close();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    //根据orderId关闭交易记录
    @Override
    public void closePayment(String orderId) {
        //update PaymentInfo ser PaymenStatus=PaymentStatus.CLOSED where orderId=?
        //paymentInfo更新的内容
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentStatus(PaymentStatus.ClOSED);
        //example 设置条件
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("orderId",orderId);
        paymentInfoMapper.updateByExampleSelective(paymentInfo,example);
    }

}




















