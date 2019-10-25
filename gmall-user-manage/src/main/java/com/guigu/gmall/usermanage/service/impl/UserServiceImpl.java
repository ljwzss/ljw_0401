package com.guigu.gmall.usermanage.service.impl;





import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.guigu.gmall.RedisUtil;
import com.guigu.gmall.bean.UserAddress;
import com.guigu.gmall.bean.UserInfo;
import com.guigu.gmall.service.UserService;
import com.guigu.gmall.usermanage.mapper.UserAddressMapper;
import com.guigu.gmall.usermanage.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    RedisUtil redisUtil;
    @Autowired
    UserAddressMapper userAddressMapper;

    //登录
    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60*24;

    //查询所有
    @Override
    public List<UserInfo> getUserInfoListAll() {
        List<UserInfo> userInfos = userMapper.selectAll();
        return userInfos;
    }
    //添加
    @Override
    public void addUser(UserInfo userInfo) {
        userMapper.insertSelective(userInfo);
    }
    // 修改
    @Override
    public void updateUser(UserInfo userInfo) {
    userMapper.updateByPrimaryKeySelective(userInfo); //按条件修改
    }

    //根据名称修改
    @Override
    public void updateUserByName(String name, UserInfo userInfo) {
        Example example=new Example(UserInfo.class);
        //条件  等于什么什么
        example.createCriteria().andEqualTo("name",name);
        userMapper.updateByExampleSelective(userInfo,example);
    }
    //删除
    @Override
    public void delUser(UserInfo userInfo) {
        userMapper.deleteByPrimaryKey(userInfo.getId());
    }
    //查某一个用户
    @Override
    public UserInfo getUseInfoById(String id) {

        return userMapper.selectByPrimaryKey(id);
    }



    @Override
    public UserInfo login(UserInfo userInfo) {
    // 先获取密码 将密码  变成加密之后的
        String passwd = userInfo.getPasswd();
        String md5Passwd = DigestUtils.md5DigestAsHex(passwd.getBytes());
        //放入
        userInfo.setPasswd(md5Passwd);
        //查询数据
        UserInfo info = userMapper.selectOne(userInfo);
        if(info!=null){
            Jedis jedis=null;
            try {
                //获取到redis 将信息存储到redis
                jedis = redisUtil.getJedis();
                //使用redis 必须定义key
                jedis.setex(userKey_prefix+info.getId()+userinfoKey_suffix,userKey_timeOut,JSON.toJSONString(info));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(jedis!=null){
                    jedis.close();
                }
            }
            return info;
        }
        return null;
    }

    //认证方式
    @Override
    public UserInfo verify(String userId) {
        UserInfo userInfo=null;
        Jedis jedis = redisUtil.getJedis();
        //创建key
        String userkey=userKey_prefix+userId+userinfoKey_suffix;
        String userJson = jedis.get(userkey);
        //延迟时效
        jedis.expire(userkey,userKey_timeOut);
        if(userJson!=null){
            //转为对象
            userInfo=JSON.parseObject(userJson,UserInfo.class);
        }
        jedis.close();
        return userInfo;
    }

    //订单类
    @Override
    public List<UserAddress> getUserAddressByUserId(String userId) {
        Example example = new Example(UserAddress.class);
        example.createCriteria().andEqualTo("userId",userId);

        return userAddressMapper.selectByExample(example);
    }

    @Override
    public List<UserAddress> getUserAddressByUserId(UserAddress userAddress) {

        return userAddressMapper.select(userAddress);
    }


}














