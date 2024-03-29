package com.guigu.gmall.manage.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.guigu.gmall.RedisUtil;
import com.guigu.gmall.bean.*;
import com.guigu.gmall.manage.constant.ManageConst;
import com.guigu.gmall.manage.mapper.*;
import com.guigu.gmall.service.ManageService;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    BaseCatalog1Mapper baseCatalog1Mapper;
    @Autowired
    BaseCatalog2Mapper baseCatalog2Mapper;
    @Autowired
    BaseCatalog3Mapper baseCatalog3Mapper;

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

    //=============================商品 spu
    //总的属性
    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    SpuInfoMapper spuInfoMapper;

    @Autowired
    SpuImageMapper  spuImageMapper;
    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
   SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    //=============================商品 sku
    @Autowired
    SkuAttrValueMapper skuAttrValueMapper; //平台属性
    @Autowired
    SkuImageMapper skuImageMapper;
    @Autowired
    SkuInfoMapper skuInfoMapper;
    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper; //销售属性
    @Autowired
    private RedisUtil redisUtil;






    @Override
    public List<BaseCatalog1> getCatalog1() {

        return baseCatalog1Mapper.selectAll();
    }

    //在二的列表里边还有一级列表
    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 catalog2 = new BaseCatalog2();
        catalog2.setCatalog1Id(catalog1Id);
        List<BaseCatalog2> baseCatalog2s = baseCatalog2Mapper.select(catalog2);
        return baseCatalog2s;
    }
    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 catalog3 = new BaseCatalog3();
        catalog3.setCatalog2Id(catalog2Id);
        List<BaseCatalog3> baseCatalog3s = baseCatalog3Mapper.select(catalog3);
        return baseCatalog3s;
    }

    //显示不同的平台属性-------------------
    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {

//        BaseAttrInfo attrInfo = new BaseAttrInfo();
//        attrInfo.setCatalog3Id(catalog3Id);
//        List<BaseAttrInfo> infoList = baseAttrInfoMapper.select(attrInfo);
//        //查询平台属性值-------------------baseAttrInfo（属性）-baseAttrValue（属性值）
//        for (BaseAttrInfo baseAttrInfo : infoList) {
//            BaseAttrValue baseAttrValue = new BaseAttrValue();
//            baseAttrValue.setAttrId(baseAttrInfo.getId());
//            List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.select(baseAttrValue);
//            baseAttrInfo.setAttrValueList(baseAttrValueList);
//        }
        List<BaseAttrInfo> baseAttrList = baseAttrInfoMapper.getBaseAttrInfoListByCatalog3Id(catalog3Id);
        return baseAttrList;
    }

    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //有就进行更新
        if(baseAttrInfo.getId()!=null && baseAttrInfo.getId().length()>0) {
            baseAttrInfoMapper.updateByPrimaryKey(baseAttrInfo);
        }else {
            //没有就插入
            baseAttrInfo.setId(null);
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }
        //把原先的清空
        Example example = new Example(BaseAttrValue.class);
        example.createCriteria().andEqualTo("attrId",baseAttrInfo.getId());
        baseAttrValueMapper.deleteByExample(example);

        //遍例属性值--属性值
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        for (BaseAttrValue baseAttrValue : attrValueList) {
            String id = baseAttrInfo.getId();
            baseAttrValue.setId(null);
            baseAttrValue.setAttrId(id);
            baseAttrValueMapper.insertSelective(baseAttrValue);
        }
    }

    @Override
    public BaseAttrInfo getAttrInfo(String attrId) {

        // 创建属性对象
        BaseAttrInfo attrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);
        // 创建 属性值 对象
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        // 根据attrId字段查询对象
        baseAttrValue.setAttrId(attrId);
        List<BaseAttrValue> attrValues = baseAttrValueMapper.select(baseAttrValue);
        // 给属性对象中的属性值集合赋值
        attrInfo.setAttrValueList(attrValues);
        // 将属性对象返回
        return attrInfo;
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {

        return baseSaleAttrMapper.selectAll();
    }

    //保存全部 spu
    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        //spu基本信息
        spuInfoMapper.insertSelective(spuInfo);
        //图片信息
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        for (SpuImage spuImage : spuImageList) {
            spuImage.setId(null);
            spuImage.setSpuId(spuInfo.getId());
            spuImageMapper.insertSelective(spuImage);
        }
        //销售属性  删除，插入
        SpuSaleAttr spuSaleAttr = new SpuSaleAttr();
        spuSaleAttr.setSpuId(spuInfo.getId());
        spuSaleAttrMapper.delete(spuSaleAttr);
        //销售属性值 删除，插入
        SpuSaleAttrValue spuSaleAttrValue = new SpuSaleAttrValue();
        spuSaleAttrValue.setSpuId(spuInfo.getId());
        spuSaleAttrValueMapper.delete(spuSaleAttrValue);

        // 添加销售属性
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        for (SpuSaleAttr saleAttr : spuSaleAttrList) {
            saleAttr.setId(null);
            saleAttr.setSpuId(spuInfo.getId());
            spuSaleAttrMapper.insertSelective(saleAttr);
            // 添加销售属性值 --跟属性时嵌套关系
            List<SpuSaleAttrValue> spuSaleAttrValueList = saleAttr.getSpuSaleAttrValueList();
            // 注意这种循环 里 加循环   小量数据可以
            for (SpuSaleAttrValue saleAttrValue : spuSaleAttrValueList) {
                saleAttrValue.setId(null);
                saleAttrValue.setSpuId(spuInfo.getId());
                spuSaleAttrValueMapper.insertSelective(saleAttrValue);
            }
        }

    }

    ////根据3级分类列表查询属性的展示
    @Override
    public List<SpuInfo> getSpuList(String catalog3Id) {
        SpuInfo info = new SpuInfo();
        info.setCatalog3Id(catalog3Id);
        return spuInfoMapper.select(info);
    }
    // 根据spuId获取spuImage中的所有图片列表
    @Override
    public List<SpuImage> getSpuImageList(String spuId) {
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);
        return spuImageMapper.select(spuImage);
    }

    //销售属性-------自己在写一个mapper,不用通用mapper
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {

        return spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
    }


    // 保存全部 sku---------------------
    @Override
    @Transactional //事物
    public void saveSpuInfo(SkuInfo skuInfo) {
      //基本信息
        if(skuInfo.getId()==null || skuInfo.getId().length()==0){
            // 设置id 为自增
            skuInfo.setId(null);
            skuInfoMapper.insertSelective(skuInfo);
        }else {
            skuInfoMapper.updateByPrimaryKeySelective(skuInfo);
        }
      //图片信息--先删后加
        SkuImage skuImage1 = new SkuImage();
        skuImage1.setSkuId(skuInfo.getId());
        skuImageMapper.delete(skuImage1);

        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        for (SkuImage skuImage : skuImageList) {
            skuImage.setId(null);
            skuImage.setSkuId(skuInfo.getId());
            skuImageMapper.insertSelective(skuImage);
        }
        //平台属性
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuInfo.getId());
        skuAttrValueMapper.delete(skuAttrValue);

        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue attrValue : skuAttrValueList) {
            attrValue.setId(null);
            attrValue.setSkuId(skuInfo.getId());
            skuAttrValueMapper.insertSelective(attrValue);
        }
        //销售属性
        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuInfo.getId());
        skuSaleAttrValueMapper.delete(skuSaleAttrValue);

        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue saleAttrValue : skuSaleAttrValueList) {
            saleAttrValue.setId(null);
            saleAttrValue.setSkuId(skuInfo.getId());
            skuSaleAttrValueMapper.insertSelective(saleAttrValue);
        }


    }

    //根据  skuid 查询skuInfo 商品详情页展示
    @Override
    public SkuInfo getSkuInfo(String skuId) {
        return getSkuInfoRedisson(skuId);
        //使用 redis--set命令做分布锁
        //return getSkuInfoRedist(skuId);
    }

    private SkuInfo getSkuInfoRedisson(String skuId) {
        //业务代码 ------------------这种事 Redisson
        SkuInfo skuInfo=null;
        Jedis jedis=null;
        RLock lock=null;
        //测试redis string
        try {
            jedis = redisUtil.getJedis();
//        jedis.set("age","18");
//        jedis.close();
            //先判断  存在的话 从redis中取 不存在从数据库中取出来 放到redis中
            //定义key

            String skuInfoKey= ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX;
            if(jedis.exists(skuInfoKey)){
                //取出数据
                    String skuInfoJson = jedis.get(skuInfoKey);
                if(skuInfoJson!=null && skuInfoJson.length()!=0){
                    //将数据转换成对象
                    skuInfo= JSON.parseObject(skuInfoJson, SkuInfo.class);
                    return skuInfo;
                }
            }else {
                //创建Config
                Config config = new Config();
                config.useSingleServer().setAddress("redis://192.168.152.134:6379");

                RedissonClient redisson = Redisson.create(config);

                lock = redisson.getLock("my-lock");

                lock.lock(10, TimeUnit.SECONDS);


                //从数据库中获取 并且存放到redis
                skuInfo=getSkuInfoDB(skuId);
                String toJSONString = JSON.toJSONString(skuInfo);
                //redis分布式锁  setnx  setex
                jedis.setex(skuInfoKey,ManageConst.SKUKEY_TIMEOUT,toJSONString);
                return skuInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(jedis!=null){
                jedis.close();
            }
            if(lock!=null){
                lock.unlock();
            }
        }
        //上边都不走 从db走
        return getSkuInfoDB(skuId);
    }

    //ctrl+alt+m  提取方法----------------------分布式锁

    private SkuInfo getSkuInfoRedis(String skuId) {
        SkuInfo skuInfo=null;
        Jedis jedis=null;
        //测试redis string
        try {
             jedis = redisUtil.getJedis();
//        jedis.set("age","18");
//        jedis.close();
            //先判断  存在的话 从redis中取 不存在从数据库中取出来 放到redis中
            //定义key

            String skuInfoKey= ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX;
            if(jedis.exists(skuInfoKey)){
                //取出数据
                String skuInfoJson = jedis.get(skuInfoKey);
                if(skuInfoJson!=null && skuInfoJson.length()!=0){
                    //将数据转换成对象
                        skuInfo= JSON.parseObject(skuInfoJson, SkuInfo.class);
                        return skuInfo;
                }
            }else {
                //从数据库中获取 并且存放到redis
                skuInfo=getSkuInfoDB(skuId);
                String toJSONString = JSON.toJSONString(skuInfo);
                //redis分布式锁  setnx  setex
                jedis.setex(skuInfoKey,ManageConst.SKUKEY_TIMEOUT,toJSONString);
                return skuInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(jedis!=null){
            jedis.close();
            }
        }
        //上边都不走 从db走
        return getSkuInfoDB(skuId );
    }

    private SkuInfo getSkuInfoDB(String skuId) {
        //单纯的信息
        //通过skuid 将skuImageList查询出来直接放入skuInfo对象中
        SkuInfo skuInfo;
        skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        skuInfo.setSkuImageList(getSkuImageBySkuId(skuId));

        //通过skuId 获取skuAttrValue数据
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.select(skuAttrValue);
        skuInfo.setSkuAttrValueList(skuAttrValueList);
        return skuInfo;
    }


    //通过skuid 将 skuImageList查询出来放到 skuInfo对象中----原先的
//        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
//        //
//        if(skuInfo!=null){
//             //查询图片
//            SkuImage skuImage = new SkuImage();
//            skuImage.setSkuId(skuId);
//            //
//            List<SkuImage> select = skuImageMapper.select(skuImage);
//            //将查询出来的所有图片赋予对象
//            skuInfo.setSkuImageList(select);
//            return skuInfo;
//        }
//        return null;

    @Override
    public List<SkuImage> getSkuImageBySkuId(String skuId) {
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);

        return skuImageMapper.select(skuImage);
    }
    //获取销售属性结果集
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo) {

        return   spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuInfo.getId(),skuInfo.getSpuId());
    }

    ////根据spuid查询一有的sku涉及的销售属性清单
    @Override
    public Map getSkuValueIdsMap(String spuId) {
        List<Map> mapList = skuSaleAttrValueMapper.getSaleAttrValuesBySpu(spuId);
         Map skuValueIdsMap = new HashMap<>();

        for (Map map : mapList) {
          String skuId =(Long ) map.get("sku_id") +"";
          String valueIds=(String)map.get("value_ids");
          skuValueIdsMap.put(valueIds,skuId);
        }
        return skuValueIdsMap;
    }

    //通过平台属性值id查询平台属性，平台属性值 81,82,83,147,148
    @Override
    public List<BaseAttrInfo> getAttrList(List<String> attrValueIdList) {
        //使用工具类apaqi 的   进行转换
        String valueIds = StringUtils.join(attrValueIdList.toArray(), ",");
        System.out.println("valueIds:"+valueIds);
        return baseAttrInfoMapper.selectAttrInfoListByIds(valueIds);
    }
}







