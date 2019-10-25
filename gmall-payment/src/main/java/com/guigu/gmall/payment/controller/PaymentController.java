package com.guigu.gmall.payment.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;

import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.guigu.gmall.bean.OrderInfo;
import com.guigu.gmall.bean.PaymentInfo;
import com.guigu.gmall.bean.enums.PaymentStatus;
import com.guigu.gmall.payment.config.AlipayConfig;
import com.guigu.gmall.payment.config.IdWorker;
import com.guigu.gmall.service.OrderService;
import com.guigu.gmall.service.PaymentService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


import javax.servlet.http.HttpServletResponse;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.apache.catalina.manager.Constants.CHARSET;

@Controller
public class PaymentController {

    @Reference
    private OrderService orderService;

    @Reference
    private PaymentService paymentService;

    @Autowired
    private  AlipayClient alipayClient;

    //是从order-web 跳转过来
    @RequestMapping("index")
    //@LoginRequire
    public String index(HttpServletRequest request){

        String orderId = request.getParameter("orderId");
        //存储总金额 totalAmount
        OrderInfo orderInfo=orderService.getOrderInfo(orderId);
        request.setAttribute("totalAmount",orderInfo.getTotalAmount());
        //存储订单id
        request.setAttribute("orderId",orderId);
        return "index";
    }

    @RequestMapping("alipay/submit")
    @ResponseBody
    public String alipaySubmit(HttpServletRequest request, HttpServletResponse response){
        //数据保存 将交易信息保存到paymentInfo
        PaymentInfo paymentInfo = new PaymentInfo();
        //paymentInfo 数据来源于orderId
        String orderId = request.getParameter("orderId");
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        // 保存支付信息
        paymentInfo.setOrderId(orderId);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject("-------------");
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        paymentInfo.setCreateTime(new Date());
        //保存信息
        paymentService.savyPaymentInfo(paymentInfo);
        //根据orderId查询数据

        //显示二维码


        //AlipayClient 注入到容器中
       // AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE); //获得初始化的AlipayClient
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        //同步回调
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
       //异步回调
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址
       //设置参数
        // 声明一个Map
        Map<String,Object> map=new HashMap<>();
        map.put("out_trade_no",paymentInfo.getOutTradeNo());
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("subject",paymentInfo.getSubject());
        map.put("total_amount",paymentInfo.getTotalAmount());
        // 将map变成json
        String Json = JSON.toJSONString(map);
        alipayRequest.setBizContent(Json);
//        alipayRequest.setBizContent("{" +
//                "    \"out_trade_no\":\"20150320010101001\"," +
//                "    \"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
//                "    \"total_amount\":88.88," +
//                "    \"subject\":\"Iphone6 16G\"," +
//                "    \"body\":\"Iphone6 16G\"," +
//                "    \"passback_params\":\"merchantBizType%3d3C%26merchantBizNo%3d2016010101111\"," +
//                "    \"extend_params\":{" +
//                "    \"sys_service_provider_id\":\"2088511833207846\"" +
//                "    }"+
//                "  }");//填充业务参数
        String form="";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        response.setContentType("text/html;charset=" + CHARSET);
//        response.getWriter().write(form);//直接将完整的表单html输出到页面
//        response.getWriter().flush();
//        response.getWriter().close();
        //15秒执行一次，总共需要执行3次。发送一个队列
        paymentService.sendDelayPaymentResult(paymentInfo.getOutTradeNo(),15,3);
        return  form;
    }
    //同步回调
    @RequestMapping("alipay/callback/return")
    public String callbackReturn(){
        //返回订单界面
        //情况购物车
        return "redirect:"+AlipayConfig.return_order_url;
    }
    //异步回调
    @RequestMapping("alipay/callback/notify")
    public  String callbackNotify(@RequestParam Map<String,String> paramMap, HttpServletRequest request){
        //Map<String, String> paramsMap = ... //将异步通知中收到的所有参数都存放到map中
        boolean flag = false; //调用SDK验证签名
        try {
            flag = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key,"utf-8",AlipayConfig.sign_type);
            //交易状态
            String trade_status=paramMap.get("trade_status");
            //获取交易编号
            String out_trade_no = paramMap.get("out_trade_no");

            if(flag){
                // TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
                //查单据是否处理
                if("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)){
                    //进一步判断 调用服务层
                    PaymentInfo paymentInfoQuery = new PaymentInfo();
                    paymentInfoQuery.setOutTradeNo(out_trade_no);
                    PaymentInfo paymentInfo=paymentService.getPaymentInfo(paymentInfoQuery);

                    if (paymentInfo.getPaymentStatus()==PaymentStatus.PAID || paymentInfo.getPaymentStatus()==PaymentStatus.ClOSED){
                        return "failure";
                    }
                    //支付成功应该修改交易记录

                    // 修改
                    PaymentInfo paymentInfoUpd = new PaymentInfo();
                    // 设置状态
                    paymentInfoUpd.setPaymentStatus(PaymentStatus.PAID);
                    // 设置创建时间
                    paymentInfoUpd.setCallbackTime(new Date());
                    // 设置内容
                    paymentInfoUpd.setCallbackContent(paramMap.toString());
                    //更新交易记录
                    paymentService.updatePaymentInfo(out_trade_no,paymentInfoUpd);
                    //支付成功 订单状态变成支付 发送消息队列
                    paymentService.sendPaymentResult(paymentInfoQuery,"result");
                    return "success";
                }
            }else{
                // TODO 验签失败则记录异常日志，并在response中返回failure.
                return "failure";
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
     return "failure";
     }


     //alipay.trade.refund？orderId=101 退款
    @RequestMapping("refund")
    @ResponseBody
    public String refund(String orderId){
    boolean result=paymentService.refund(orderId);

        return ""+result;
    }

    /*
    class Student{
    private int id: id=1
    private String name :name=admin
    }
    没有实体类就用map
    Map map=new HashMap();
    map.put(id,1)
    map.put(name.admin)
     */
    //微信
    @RequestMapping("wx/submit")
    @ResponseBody
    public Map createNative(HttpServletRequest request){
      //  String orderId = request.getParameter("orderId");
        //IdWorker 编号
        IdWorker idWorker = new IdWorker();
        long id = idWorker.nextId();
        // 调用服务从 固定值为1
        Map map=paymentService.createNative(id+"","1");
        System.out.println(map.get("code_url"));
        return map;
    }

    // 发送验证  payment.gamll.com/sendPaymentResult?orderId=xxx&result=xxx 消费者
    @RequestMapping("sendPaymentResult")
    @ResponseBody
    public String sendPaymentResult(PaymentInfo paymentInfo,@RequestParam("result") String result){
        paymentService.sendPaymentResult(paymentInfo,result);
        return "sent payment result";
    }

    // 查询订单信息--查询验证  加载
    //查询当前交易是否已经付款   --由于某些原因，不能确定支付成功的时候，可以联系客服
    //payment.gmall.com/queryPaymentResult?orderId=xxx  不是要orderId 而是要 out_trade_no
    @RequestMapping("queryPaymentResult")
    @ResponseBody
    public String queryPaymentResult(PaymentInfo paymentInfo){
        //根据需要的orderId查询出整个paymentInfo 对象
        //select * from paymentInfo where orderId=?
        PaymentInfo paymentInfoQuery = paymentService.getPaymentInfo(paymentInfo);
        boolean result=false;
        if(paymentInfoQuery!=null){
            //需要outTradeNo 和 orderId
            result = paymentService.checkPayment(paymentInfoQuery);
        }
        return ""+result;
    }
}













