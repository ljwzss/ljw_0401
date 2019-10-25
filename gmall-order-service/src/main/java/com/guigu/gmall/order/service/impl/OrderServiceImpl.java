package com.guigu.gmall.order.service.impl;



import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.guigu.gmall.RedisUtil;
import com.guigu.gmall.bean.OrderDetail;
import com.guigu.gmall.bean.OrderInfo;
import com.guigu.gmall.bean.enums.OrderStatus;
import com.guigu.gmall.bean.enums.ProcessStatus;
import com.guigu.gmall.config.ActiveMQUtil;
import com.guigu.gmall.order.mapper.OrderDetailMapper;
import com.guigu.gmall.order.mapper.OrderInfoMapper;
import com.guigu.gmall.service.OrderService;
import com.guigu.gmall.service.PaymentService;
import com.guigu.gmall.util.HttpClientUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import javax.jms.Queue;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderInfoMapper orderInfoMapper;
    //订单详情
    @Autowired
    OrderDetailMapper orderDetailMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Reference
    PaymentService paymentService;
    //提交订单
    @Override
    @Transactional
    public String saveOrder(OrderInfo orderInfo) {
        //数据库表结构 orderInfo orderDerail
        //总金额 订单状态 userId 第三方交易编号 创建时间 过期时间 进程状态
        orderInfo.sumTotalAmount();
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        // 生成第三方支付编号
        String outTradeNo="ATGUIGU"+System.currentTimeMillis()+""+new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        //创建时间
        orderInfo.setCreateTime(new Date());
        //过期时间 为下订单之后的1天
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,1);
        orderInfo.setExpireTime(calendar.getTime());
        //进程状态
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        //orderInfo
        orderInfoMapper.insertSelective(orderInfo);
        // 插入订单详细信息
        List<OrderDetail> orderDetailListt = orderInfo.getOrderDetailList();
        //可以来个判断
        if(orderDetailListt!=null && orderDetailListt.size()>0) {
            for (OrderDetail orderDetail : orderDetailListt) {
                //防止提交id重复
                orderDetail.setId(null);
                // 设置订单Id  实体类上必须有注解
                orderDetail.setOrderId(orderInfo.getId());
                orderDetailMapper.insertSelective(orderDetail);
            }
        }
        // 为了跳转到支付页面使用。支付会根据订单id进行支付。

        return orderInfo.getId();
    }

    //生成流水号
    @Override
    public String getTradeNo(String userId) {

        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey="user:"+userId+":tradeCode";
        //定义流水号
        String tradeCode = UUID.randomUUID().toString();
        jedis.setex(tradeNoKey,10*60,tradeCode);
        jedis.close();
        return tradeCode;
    }
    //判断流水号和缓存中流水号
    @Override
    public boolean checkTradeCode(String userId, String tradeCodeNo) {

        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey = "user:"+userId+":tradeCode";
        String tradeCode = jedis.get(tradeNoKey);
        jedis.close();
        if (tradeCode!=null && tradeCode.equals(tradeCodeNo)){
            return  true;
        }else{
            return false;
        }
    }

    //删除流水号
    @Override
    public void delTradeCode(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey =  "user:"+userId+":tradeCode";
        jedis.del(tradeNoKey);
        jedis.close();
    }

    //验证库存 http://www.gware.com
    @Override
    public boolean checkStock(String skuId, Integer skuNum) {

        String result = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + skuId + "&num=" + skuNum);
        return "1".equals(result);
    }


    //支付
    @Override
    public OrderInfo getOrderInfo(String orderId) {
        //通过orderid获取数据
        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);
        //跟下边库存更新 相对接----获取orderDetail 集合放入orderinfo
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);
        List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetail);
        orderInfo.setOrderDetailList(orderDetailList);

        return orderInfo;
    }
    //订单更新
    @Override
    public void updateOrderStatus(String orderId, ProcessStatus processStatus) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setProcessStatus(processStatus);
        orderInfo.setOrderStatus(processStatus.getOrderStatus());
        orderInfoMapper.updateByPrimaryKeySelective(orderInfo);
    }

    //更新时候通知库存 减库存---在库存课件里
    @Override
    public void sendOrderStatus(String orderId) {
    //创建连接并打开
        Connection connection = activeMQUtil.getConnection();
        //获取json串
        String orderJson = initWareOrder(orderId);
        try {
            connection.start();
            //创建session 并开启事务
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            //创建对象
            Queue queue = session.createQueue("ORDER_RESULT_QUEUE");
            //创建消息对象
            ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage(); //写个方法传递参数
            activeMQTextMessage.setText(orderJson);
            //创建消息提供者
            MessageProducer producer = session.createProducer(queue);
            //发送消息
            producer.send(activeMQTextMessage);
            //提交 关闭
            session.commit();
            producer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            //消息队列名称
        }
    }

    //查询所有过期的订单--------------------
    @Override
    public List<OrderInfo> getExpiredOrderList() {
        //过期时间字段expireTime 小于 当前时间 和 未支付
        Example example = new Example(OrderInfo.class);
        //构建查询条件                                                                                                     未支付
        example.createCriteria().andLessThan("expireTime",new Date()).andEqualTo("processStatus",ProcessStatus.UNPAID);

        List<OrderInfo> orderInfos = orderInfoMapper.selectByExample(example);
        return orderInfos;
    }
    //处理过期订单----------------
    @Override
    @Async
    public void execExpiredOrder(OrderInfo orderInfo) {
        //更新 状态
        updateOrderStatus(orderInfo.getId(),ProcessStatus.CLOSED);
        //关闭交易记录信息
        // 付款信息
        paymentService.closePayment(orderInfo.getId());
    }

    //发送的数据
    private String initWareOrder(String orderId) {
        //根据orderId查询orderInfo
        OrderInfo orderInfo = getOrderInfo(orderId);
        //将oderInfo转化为map 同时获取数据
        Map map=initWareOrder(orderInfo);
        return JSON.toJSONString(map);
    }
  // 设置初始化仓库信息方法 转化----------注意 之前是private 现在 是public 接口方法以加，是做拆单功能
    public Map initWareOrder(OrderInfo orderInfo) {
        Map<String,Object> map = new HashMap<>();
        map.put("orderId",orderInfo.getId());
        map.put("consignee", orderInfo.getConsignee());
        map.put("consigneeTel",orderInfo.getConsigneeTel());
        map.put("orderComment",orderInfo.getOrderComment());
        //map.put("orderBody",orderInfo.getTradeBody());
        map.put("orderBody","测试-----");
        map.put("deliveryAddress",orderInfo.getDeliveryAddress());
        map.put("paymentWay","2");
        map.put("wareId",orderInfo.getWareId());

        // 组合json
        List detailList = new ArrayList();//封装的集合
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        if(orderDetailList!=null && orderDetailList.size()>0) {
            for (OrderDetail orderDetail : orderDetailList) {
                Map detailMap = new HashMap();

                detailMap.put("skuId", orderDetail.getSkuId());
                detailMap.put("skuName", orderDetail.getSkuName());
                detailMap.put("skuNum", orderDetail.getSkuNum());
                //因为数据不单单就一个  所以封装成一个集合
                detailList.add(detailMap);
            }
        }
        map.put("details",detailList);
        return map;
    }
    //拆单----
    @Override
    public List<OrderInfo> orderSplit(String orderId, String wareSkuMap) {
        /*获取原始订单          key v 的形式
          需要将wareSkuMap    [{"wareId":"1","skuIds":["2","10"]},{"wareId":"2","skuIds":["3"]}]中的数据判断是否需要拆单并写拆单规则
          wareSkuMap转换为我们能操做的对象
          创建新的自订单
          保存子订单到数据库
          把子订单添加到集合List<orderInfo>
          更新原始订单的状态
        */

        List<OrderInfo>subOrderInfoList=new ArrayList<>();

        OrderInfo orderInfoOrigin = getOrderInfo(orderId);
        List<Map> maps = JSON.parseArray(wareSkuMap, Map.class);
        if (maps!=null && maps.size()>0){
            for (Map map : maps) {
                //获取数据
                String wareId = (String) map.get("wareId");//仓库id
                List<String> skuIds = (List<String>) map.get("skuIds");
                // 创建新的子订单
                OrderInfo subOrderInfo = new OrderInfo();
                //属性赋值
                BeanUtils.copyProperties(orderInfoOrigin,subOrderInfo);
                //防止主键冲突
                subOrderInfo.setId(null);
                //声明一个集合来存储子订单明细
                ArrayList<OrderDetail> orderDetailsList = new ArrayList<>();
                // 价格 ：必须有订单详情
                List<OrderDetail> orderDetailList = orderInfoOrigin.getOrderDetailList();
                if(orderDetailList!=null && orderDetailList.size()>0){
                    for (OrderDetail orderDetail : orderDetailList) {
                        for(String skuId:skuIds){
                         if(orderDetail.getSkuId().equals(skuId)) {
                            //新的子订单明细
                             orderDetailsList.add(orderDetail);
                          }
                       }
                    }
                }
                subOrderInfo.setOrderDetailList(orderDetailList);
                //计算总价格
                subOrderInfo.sumTotalAmount();
                //赋值父订单id
                subOrderInfo.setParentOrderId(orderId);
                //赋值仓库id
                subOrderInfo.setWareId(wareId);

                //保存子订单 到数据库
                saveOrder(subOrderInfo);
                //添加子订单
                subOrderInfoList.add(subOrderInfo);
            }
        }
        updateOrderStatus(orderId,ProcessStatus.SPLIT);
        return subOrderInfoList;
    }

}















