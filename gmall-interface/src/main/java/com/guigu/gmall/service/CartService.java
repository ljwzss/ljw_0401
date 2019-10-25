package com.guigu.gmall.service;

import com.guigu.gmall.bean.CartInfo;

import java.util.List;

//购物车
public interface CartService {
    //返回值  参数列表  数量 用户id 商品数量
    public  void  addToCart(String skuId,String userId,Integer skuNum);
    //根据用户id 查询购物车信息
    List<CartInfo> getCartList(String userId);

    //合并
    List<CartInfo> mergeToCartList(List<CartInfo> cartListCK, String userId);

    public List<CartInfo> loadCartCache(String userId);
    //选中商品
    void checkCart(String skuId, String isChecked, String userId);

    //根据用户id去查询选中商品
    List<CartInfo> getCartCheckedList(String userId);
}
