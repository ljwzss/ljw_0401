package com.guigu.gmall.service;

import com.guigu.gmall.bean.*;

import java.util.List;
import java.util.Map;

//三级列表 只需要查询就行
public interface ManageService {

    //查询一级分类
    public List<BaseCatalog1> getCatalog1();
    //查询二级分类
    public List<BaseCatalog2> getCatalog2(String catalog1Id);
    //查询三级分类
    public List<BaseCatalog3> getCatalog3(String catalog2Id);
    //显示不同的平台属性
    public List<BaseAttrInfo> getAttrList(String catalog3Id);

    //保存----------
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo);
    //取到属性值列表   修改你保存的信息
    BaseAttrInfo getAttrInfo(String attrId);
    //删除

    //获得常用销售属性--------------商品
    public List<BaseSaleAttr>getBaseSaleAttrList();
    //保存spu信息
    public void saveSpuInfo(SpuInfo spuInfo);

    //根据3级分类列表查询属性的展示
    public List<SpuInfo> getSpuList(String catalog3Id);

    // 根据spuId获取spuImage中的所有图片列表
    public List<SpuImage> getSpuImageList(String spuId);

    //销售属性
    List<SpuSaleAttr> getSpuSaleAttrList(String spuId);


    //保存skuinfo
   public  void saveSpuInfo(SkuInfo skuInfo);

   //根据  skuid 查询skuInfo 商品详情页展示--------
    SkuInfo getSkuInfo(String skuId);

    //图片列表
    List<SkuImage> getSkuImageBySkuId(String skuId);
    //获取销售属性结果集
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo);

    //根据spuid查询一有的sku涉及的销售属性清单
    public Map getSkuValueIdsMap(String spuId);



}
