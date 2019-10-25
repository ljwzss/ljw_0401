package com.guigu.gmall.cart.mapper;

import com.guigu.gmall.bean.CartInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CartInfoMapper extends Mapper<CartInfo> {

    //根据userId查询数据
    List<CartInfo> selectCartListWithCurPrice(String userId);
}
