package com.guigu.gmall.usermanage.service.impl;





import com.alibaba.dubbo.config.annotation.Service;
import com.guigu.gmall.bean.UserInfo;
import com.guigu.gmall.service.UserService;
import com.guigu.gmall.usermanage.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;

import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;

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
}
