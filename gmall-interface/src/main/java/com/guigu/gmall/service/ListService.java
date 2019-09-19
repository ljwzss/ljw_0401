package com.guigu.gmall.service;

import com.guigu.gmall.bean.SkuLsInfo;
import com.guigu.gmall.bean.SkuLsParams;
import com.guigu.gmall.bean.SkuLsResult;

//全文检索 商品上架
public interface ListService {

    public void saveSkuLsInfo(SkuLsInfo skuLsInfo);

    //检索后返回一个结果集
    public SkuLsResult search(SkuLsParams skuLsParams);

    //更新商品的热度
    public void incrHotScore(String skuId);
}
