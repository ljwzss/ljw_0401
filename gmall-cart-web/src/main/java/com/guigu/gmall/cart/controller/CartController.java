package com.guigu.gmall.cart.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.guigu.gmall.bean.CartInfo;
import com.guigu.gmall.bean.SkuInfo;
import com.guigu.gmall.config.CookieUtil;
import com.guigu.gmall.config.LoginRequire;
import com.guigu.gmall.service.CartService;
import com.guigu.gmall.service.ManageService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

//购物车
@Controller
public class CartController {

    @Reference
    CartService cartService;

    @Autowired
    private CartCookieHandler cartCookieHandler;

    @Reference
    private ManageService manageService;

    //购物车是从商品详情页过来的 item-web
    //控制器 item.html中找
    @RequestMapping("addToCart")
    @LoginRequire(autoRedirect = false)//添加购物车不需要登录
    public String addToCart(HttpServletRequest request, HttpServletResponse response){

        //对相应的商品信息做一个保存

        //调用服务层将商品数据天加到redis mysql

        String userId = (String) request.getAttribute("userId");
        //获取购买的 数量 和 商品id 和用户id
        String skuNum = request.getParameter("skuNum");
        String skuId = request.getParameter("skuId");

        //判断用户是否登录
        if(userId!=null){
            //登陆了
            cartService.addToCart(skuId,userId,Integer.parseInt(skuNum));
            cartService.loadCartCache(userId);
        }else {
            //没登录            放到cookie中
            cartCookieHandler.addToCart(request,response,skuId,userId,Integer.parseInt(skuNum));
        }
        //通过skuid 查询skuinfo
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        request.setAttribute("skuNum",skuNum);
        request.setAttribute("skuInfo",skuInfo);
        return "success";
    }

    //展示购物车列表
    @RequestMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public  String cartList(HttpServletRequest request,HttpServletResponse response) {

        List<CartInfo>cartInfoList=new ArrayList<>();
        //获取userId
        String userId = (String) request.getAttribute("userId");
        if(userId!=null){
            //先看未登录购物车中是否有数据
            List<CartInfo> cartListCK = cartCookieHandler.getCartList(request);
            if(cartListCK!=null && cartListCK.size()>0){
                //合并购物车
                cartInfoList= cartService.mergeToCartList(cartListCK,userId);
                //删除未登录的数据
                cartCookieHandler.deleteCartCookie(request,response);
            }else {
                //从redis 或者 数据库中查询
                cartInfoList=cartService.getCartList(userId);
            }
        }else {
            //cookie  这用这个是因为你用CookieUtil 获取同样也是 需要request
            //CookieUtil.getCookieValue();
            cartInfoList=cartCookieHandler.getCartList(request);
        }
        //保存购物车集合
        request.setAttribute("cartInfoList",cartInfoList);
        return "cartList";
    }

    //购物车选中商品
    @RequestMapping("checkCart")
    @ResponseBody
    @LoginRequire(autoRedirect = false)
    public void checkCart(HttpServletRequest request,HttpServletResponse response) {
        //从页面获取传递过来的数据
        String skuId = request.getParameter("skuId");
        String isChecked = request.getParameter("isChecked");
        String userId = (String) request.getAttribute("userId");

        if(userId!=null){
            //登录时选中 redis
            cartService.checkCart(skuId,isChecked,userId);
        }else {
            //未登录  cookie
            cartCookieHandler.checkCart(request,response,skuId,isChecked);
        }
    }

    //点击去结算重定向到订单页面
    @RequestMapping("toTrade")
    @LoginRequire(autoRedirect = true)
    public String toTrade(HttpServletRequest request,HttpServletResponse response) {
        //获取userId
        String userId = (String) request.getAttribute("userId");
        //合并购物车中勾选的商品cookie--redis合并
        //获取未登录数据
        List<CartInfo> cartListCK = cartCookieHandler.getCartList(request);
        if(cartListCK!=null && cartListCK.size()>0){
            //合并勾选的商品
            cartService.mergeToCartList(cartListCK,userId);
            //删除未登录的数据
            cartCookieHandler.deleteCartCookie(request,response);
        }
        return "redirect://trade.gmall.com/trade";
    }
}












