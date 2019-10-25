package com.guigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.guigu.gmall.RedisUtil;
import com.guigu.gmall.bean.CartInfo;
import com.guigu.gmall.bean.SkuInfo;
import com.guigu.gmall.cart.constant.CartConst;
import com.guigu.gmall.cart.mapper.CartInfoMapper;
import com.guigu.gmall.service.CartService;
import com.guigu.gmall.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    //商品详情
    @Autowired
    CartInfoMapper cartInfoMapper;

    @Reference
    ManageService manageService;

    @Autowired
    RedisUtil redisUtil;

    @Override
    //登录时添加购物车
    public void addToCart(String skuId, String userId, Integer skuNum) {
    /*
    判断购物车中是否有该商品
    有 数量相加
    没有：直接添加到数据库
    放入redis中
    mysql 与 redis 如何进行同步
        早添加购物车的时候，直接添加到 数据库 并添加到redis
     */
        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setUserId(userId);

        //获取jedis
        Jedis jedis = redisUtil.getJedis();
        //定义key
        String cartkey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CHECKED_KEY_SUFFIX;
        //哪种数据类型 hash jedis.hset(key field value)
        /*
        key---user:userId:cart
        field---skuId
        value----cartInfoValue
         */

        CartInfo cartInfoExist = cartInfoMapper.selectOne(cartInfo);
        if (cartInfoExist != null) {
            //数量相加
            cartInfoExist.setSkuNum(cartInfoExist.getSkuNum() + skuNum);
            //给实时价格更新
            cartInfoExist.setSkuPrice(cartInfoExist.getCartPrice());
            //cartInfoExist更新到数据库(updateByPrimaryKeySelective);
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExist);
            //放入redis
            //jedis.hset(cartkey,skuId,JSON.toJSONString(cartInfoExist));
        } else {
            //没有 直接添加到数据库 获取skuInfo信息，添加到cartInfo中
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);
            CartInfo cartInfo1 = new CartInfo();
            cartInfo1.setSkuId(skuId);
            cartInfo1.setCartPrice(skuInfo.getPrice());
            cartInfo1.setSkuPrice(skuInfo.getPrice());
            cartInfo1.setSkuName(skuInfo.getSkuName());
            cartInfo1.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo1.setUserId(userId);
            cartInfo1.setSkuNum(skuNum);
            //添加到数据库
            cartInfoMapper.insertSelective(cartInfo1);
            //放入redis
            //jedis.hset(cartkey,skuId,JSON.toJSONString(cartInfo1));
            cartInfoExist = cartInfo1;
        }
        //放入redis
        jedis.hset(cartkey, skuId, JSON.toJSONString(cartInfoExist));
        //购物车如何设置过期时间？
        //先获取到key
        String userkey = CartConst.USER_KEY_PREFIX + userId + CartConst.USERINFOKEY_SUFFIX;
        //剩余过期时间
        Long ttl = jedis.ttl(userkey);
        //赋值给购物车
        jedis.expire(cartkey, ttl.intValue());
        jedis.close();
    }

    //根据用户id 查询购物车信息 从redis里查询用户信息====
    @Override
    public List<CartInfo> getCartList(String userId) {
    /*
    获取jedis
    从redis获取数据
    有 则返回
    没有 从数据库查询（查询购物车实时价格），并放入redis
     */
        //局部变量
        List<CartInfo> cartInfoList = new ArrayList<>();

        Jedis jedis = redisUtil.getJedis();
        //定义key
        String cartkey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        //获取数据
        List<String> caetList = jedis.hvals(cartkey);
        if (caetList != null && caetList.size() > 0) {

            for (String cartJson : caetList) {
                //转换成对象
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
                //添加购物车数据
                cartInfoList.add(cartInfo);
            }

            //查询的时候按照id排序
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    //compareTo str1=abc str2=abcd
                    return o1.getId().compareTo(o2.getId());
                }
            });
            return cartInfoList;
        }else {
            //从数据库中获取数据
            cartInfoList=loadCartCache(userId);
            return cartInfoList;
        }
    }


    //根据userId 查询数据并放入缓存-----------------
    public List<CartInfo> loadCartCache(String userId) {

        /*
        根据userid 查询一下当前商品的实时价格
        cartInfo.skuPrice=skuInfo.price
        将查询出来的数据集合放入缓存
         */

        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurPrice(userId);
        if(cartInfoList!=null && cartInfoList.size()==0){
            return null;
        }

        //获取key
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        Jedis jedis = redisUtil.getJedis();

//        for (CartInfo cartInfo : cartInfoList) {
//            //每次放一条数据
//            jedis.hset(userCartKey,cartInfo.getSkuId(),JSON.toJSONString(cartInfo));
//        }
        //定义多条
        HashMap<String,String> map = new HashMap<>();
        for (CartInfo cartInfo : cartInfoList) {
            map.put(cartInfo.getSkuId(),JSON.toJSONString(cartInfo));
        }
        jedis.hmset(userCartKey,map);
        jedis.close();
        return cartInfoList;
    }


    //合并
    @Override
    public List<CartInfo> mergeToCartList(List<CartInfo> cartListCK, String userId) {
        //获取数据库中的数据
        List<CartInfo> cartInfoListDB = cartInfoMapper.selectCartListWithCurPrice(userId);
        //合并条件 商品相同时  skuid相同
        for (CartInfo cartInfoCK : cartListCK) {
            //声明一个boolean类型遍例
            boolean isMatch=false;
            //有相同的数据直接插入数据库
            for (CartInfo cartInfoDB : cartInfoListDB) {
                if(cartInfoCK.getSkuId().equals(cartInfoDB.getSkuId())){
                    //数量相加
                    cartInfoDB.setSkuNum(cartInfoCK.getSkuNum()+cartInfoDB.getSkuNum());
                    //更新
                    cartInfoMapper.updateByPrimaryKeySelective(cartInfoDB);
                    isMatch=true;
                }
            }
            //未登录的数据在数据库中没有，则直接插入数据库
            if(!isMatch){
                //未登录的时候userId为null
                cartInfoCK.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfoCK);
            }
        }
        //最后在查询一次更新之后，新添加的所有数据
        List<CartInfo> cartInfoList = loadCartCache(userId);
        //合并勾选商品 从数据库查询
        for (CartInfo cartInfoDB : cartInfoList) {
            for (CartInfo cartInfoCK : cartInfoList) {
                if(cartInfoDB.getSkuId().equals(cartInfoCK.getSkuId())){
                    //判断未登录状态为勾选状态
                    if("1".equals(cartInfoCK.getIsChecked())){
                       //判断数据库中的商品勾选为1
                       cartInfoDB.setIsChecked("1");
                    /*   //对数量进行覆盖
                       cartInfoDB.setSkuNum(cartInfoCK.getSkuNum());
                       //更新
                        cartInfoMapper.updateByPrimaryKeySelective(cartInfoDB);*/
                        //redis 发送消息队列
                       //自动勾选
                       checkCart(cartInfoDB.getSkuId(),cartInfoCK.getIsChecked(),userId);
                     }
                }
            }
        }
        return cartInfoList;
    }

    //选中商品
    @Override
    public void checkCart(String skuId, String isChecked, String userId) {
    /*
    1.将页面传递过来的商品id，与购物车中的商品id进行匹配
    2.修改Checked 的数据
    3.单独创建一个key来存储以选中商品
     */
        //锁定购物车状态
        //创建jedis
        Jedis jedis = redisUtil.getJedis();
        //定义key user：userId：cart
        String cartKey=CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        //获取选中的商品
        String cartInfoJson = jedis.hget(cartKey, skuId);
        //转换为对象
        CartInfo cartInfo = JSON.parseObject(cartInfoJson, CartInfo.class);
        cartInfo.setIsChecked(isChecked);
        //记得 必须将cartinfo 写会购物车
           jedis.hset(cartKey,skuId,JSON.toJSONString(cartInfo));

        // 重新记录被勾选的商品
        //定义key user：userId：checked
            String cartCheckedKey=CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CHECKED_KEY_SUFFIX;
            if("1".equals(isChecked)){
                jedis.hset(cartCheckedKey,skuId,JSON.toJSONString(cartInfo));
            }else {
                //把当前数据删除
                jedis.hdel(cartCheckedKey,skuId);
            }
            jedis.close();
    }

    @Override
    public List<CartInfo> getCartCheckedList(String userId) {

        //获取jedis
        Jedis jedis = redisUtil.getJedis();
        //定义key user：userId：checked
        String userCheckedKey =CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CHECKED_KEY_SUFFIX;
        List<String> cartCheckedList = jedis.hvals(userCheckedKey);
        List<CartInfo>cartInfoList= new ArrayList<>();
        for (String cartInfoJson : cartCheckedList) {
            cartInfoList.add (JSON.parseObject(cartInfoJson, CartInfo.class));
        }
        jedis.close();
        return  cartInfoList;
    }
}















