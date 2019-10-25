package com.guigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.guigu.gmall.bean.CartInfo;
import com.guigu.gmall.bean.SkuInfo;
import com.guigu.gmall.config.CookieUtil;
import com.guigu.gmall.service.ManageService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

//没有登录
@Component
public class CartCookieHandler {

    //定义购物车名称
    private String cookieCartName = "CART";
    //定义过期时间
    private int COOKIE_CART_MAXAGE=7*24*3600;
    @Reference
    ManageService manageService;

    public void addToCart(HttpServletRequest request, HttpServletResponse response, String skuId, String userId, int skuNum) {
         /*
    判断购物车中是否有该商品
    有 数量相加
    没有：直接添加到cookie
     */
        //获取购物车中所有数据，判断cookie中是否有购物车，有可能有中文，需要序列化
        String cookieValue = CookieUtil.getCookieValue(request, cookieCartName, true);

        List<CartInfo> cartInfoList=new ArrayList<>();

        //定义一个布尔类型的变量  存在 返回true 不存在定义一下
        boolean ifExist=false;

        //很多条数据 集合
        if(StringUtils.isNotEmpty(cookieValue)){
            cartInfoList = JSON.parseArray(cookieValue, CartInfo.class);
            //通过skuid去cookid中进行比较
            for (CartInfo cartInfo : cartInfoList) {
                if(cartInfo.getSkuId().equals(skuId)){
                    //有就数量 相加
                    cartInfo.setSkuNum(cartInfo.getSkuNum()+skuNum);
                    cartInfo.setSkuPrice(cartInfo.getCartPrice());
                    ifExist=true;
                }
            }
        }
        //不存在
        if(!ifExist){
            //没有
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);
            CartInfo cartInfo = new CartInfo();
            //数据赋值
            cartInfo.setSkuId(skuId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());

            cartInfo.setUserId(userId);
            cartInfo.setSkuNum(skuNum);
            cartInfoList.add(cartInfo);
            //将cartInfo放入集合
            cartInfoList.add(cartInfo);
        }
        //写入cookie
        CookieUtil.setCookie(request,response,cookieCartName,JSON.toJSONString(cartInfoList),COOKIE_CART_MAXAGE,true);
    }


    //获取购物车列表数据
    public List<CartInfo> getCartList(HttpServletRequest request) {
        List<CartInfo> cartInfoList=new ArrayList<>();
        String cookieValue = CookieUtil.getCookieValue(request, cookieCartName, true);
        if(StringUtils.isNotEmpty(cookieValue)){
            cartInfoList = JSON.parseArray(cookieValue, CartInfo.class);
        }
        return cartInfoList;
    }

    //删除已存在的数据
    public void deleteCartCookie(HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.deleteCookie(request,response,cookieCartName);
    }


    //将未登录的购物车中的商品修改为页面的选中状态
    public void checkCart(HttpServletRequest request, HttpServletResponse response, String skuId, String isChecked) {
        //先获取购物车中的结合
        List<CartInfo> cartList = getCartList(request);
        if(cartList!=null && cartList.size()>0){
            for (CartInfo cartInfo : cartList) {
                //判断购物车是否有相同的商品
                if(cartInfo.getSkuId().equals(skuId)){
                    cartInfo.setIsChecked(isChecked);
                }
            }
        }
        //存入购物车
        CookieUtil.setCookie(request,response,cookieCartName,JSON.toJSONString(cartList),COOKIE_CART_MAXAGE,true);
    }
}



















