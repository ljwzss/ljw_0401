package com.guigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.guigu.gmall.bean.*;
import com.guigu.gmall.bean.enums.OrderStatus;
import com.guigu.gmall.bean.enums.ProcessStatus;
import com.guigu.gmall.config.LoginRequire;
import com.guigu.gmall.service.CartService;
import com.guigu.gmall.service.OrderService;
import com.guigu.gmall.service.UserService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.spring.annotation.MapperScan;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class OrederController{
    //使用dubbo
    @Reference
    UserService userService;
    @Reference
    CartService cartService;
    @Reference
    OrderService orderService;

    @RequestMapping("trade") //消费者
    @LoginRequire
    public String trade(HttpServletRequest request){
        //获取哟用户id
        String userId = (String) request.getAttribute("userId");
        // 收货人地址
        List<UserAddress> userAddressesList = userService.getUserAddressByUserId(userId);
        //保存到作用域
        request.setAttribute("userAddressesList",userAddressesList);
    
        //展示送货清单:orderDetailList
        List<CartInfo> cartList = cartService.getCartCheckedList(userId);

        //声明集合来存储OrderDetail
        ArrayList<OrderDetail> orderDetailList = new ArrayList<>();
        //查询出老 给OrderDetail
        for (CartInfo cartInfo : cartList) {
            OrderDetail orderDetail = new OrderDetail();

            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());
            //添加orderDetail到集合
            orderDetailList.add(orderDetail);
        }
        //计算总金额并保存
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(orderDetailList);
        //这是 属性里OrderInfo 写好的方法
        orderInfo.sumTotalAmount();
        request.setAttribute("totalAmount",orderInfo.getTotalAmount());

        //保存orderDetil 集合 给页面渲染
        request.setAttribute("orderDetailList",orderDetailList);

        //将流水号保存到作用域中 ----页面存到隐藏域
        String tradeNo = orderService.getTradeNo(userId);

        request.setAttribute("tradeNo",tradeNo);
        return "trade";
    }

    //提交订单 key value 形式 就用对象就行
      @RequestMapping("submitOrder")
      @LoginRequire
      public String submitOrder(OrderInfo orderInfo,HttpServletRequest request){

          //获取userId
          // 检查tradeCode
          String userId = (String) request.getAttribute("userId");
          // 初始化参数
//          orderInfo.setOrderStatus(OrderStatus.UNPAID);
//          orderInfo.setProcessStatus(ProcessStatus.UNPAID);
//          orderInfo.sumTotalAmount();
          //调用服务层
          orderInfo.setUserId(userId);

          // 检查tradeCode----------检查之前判断用户是否是重复提交
          String tradeNo = request.getParameter("tradeNo");
          boolean flag = orderService.checkTradeCode(userId, tradeNo);
          if (!flag){
              request.setAttribute("errMsg","该页面已失效，请重新结算!");
              return "tradeFail";
          }

          // 验证库存 www.gware.com
          List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
          if(orderDetailList !=null && orderDetailList .size()>0){
              for (OrderDetail orderDetail : orderDetailList) {
                  // 从订单中去购物skuId，数量
                  boolean result = orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
                  if (!result){
                      request.setAttribute("errMsg",orderDetail.getSkuName()+"商品库存不足，请重新下单！");
                      return "tradeFail";
                  }
              }
          }

          // 保存数据
          String orderId = orderService.saveOrder(orderInfo);
          //删除流水号
          orderService.delTradeCode(userId);
          // 重定向到支付模块
          return "redirect://payment.gmall.com/index?orderId="+orderId;
      }


      // 拆单    http://order.gmall.com/orderSplit
        @RequestMapping("orderSplit")
        @ResponseBody
        public String orderSplit(HttpServletRequest request){
            String orderId = request.getParameter("orderId");
            //[{"wareId":"1","skuIds":["2","10"]},{"wareId":"2","skuIds":["3"]}]
            String wareSkuMap = request.getParameter("wareSkuMap");
            //调用service层方法
            List<OrderInfo> subOrderInfoList =orderService.orderSplit(orderId,wareSkuMap);

            ArrayList<Map> maps = new ArrayList<>();
            if(subOrderInfoList!=null && subOrderInfoList.size()>0){
                for (OrderInfo orderInfo : subOrderInfoList) {
                    //循环遍历 将orderInfo变为map 在变json字符串
                    Map map = orderService.initWareOrder(orderInfo);
                    maps.add(map);
                }
            }
            return JSON.toJSONString(maps);
        }
}



















