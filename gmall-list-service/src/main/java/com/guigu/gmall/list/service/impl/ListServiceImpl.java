package com.guigu.gmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.guigu.gmall.RedisUtil;
import com.guigu.gmall.bean.SkuLsInfo;
import com.guigu.gmall.bean.SkuLsParams;
import com.guigu.gmall.bean.SkuLsResult;
import com.guigu.gmall.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.*;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ListServiceImpl implements ListService {

    @Autowired
    JestClient jestClient;

    @Autowired
    RedisUtil redisUtil;

    public static final String ES_INDEX="gmall";

    public static final String ES_TYPE="SkuInfo";

    //全文检索 商品上架
    @Override
    public void saveSkuLsInfo(SkuLsInfo skuLsInfo) {
        Index build = new Index.Builder(skuLsInfo).index(ES_INDEX).type(ES_TYPE).id(skuLsInfo.getId()).build();
        try {
            DocumentResult execute = jestClient.execute(build);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public SkuLsResult search(SkuLsParams skuLsParams) {
    /*
     定义dsl语句
     定义执行的动作
     执行动作
     返回结果
     */
       //生成动态del语句
        String query=makeQueryStringForSearch(skuLsParams);
        Search search = new Search.Builder(query).addIndex(ES_INDEX).addType(ES_TYPE).build();
        SearchResult searchResult=null
                ;
        try {
          searchResult = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //所有的返回值都在----》    skuLsResult
        //第一个参数查询的结果集，第二个参数用户输入参数查询的实体类
        SkuLsResult skuLsResult = makeResultForSearch(searchResult,skuLsParams);
        return skuLsResult;
    }

    // 商品的  热度-------------------------------
    @Override
    public void incrHotScore(String skuId) {
        //更新次数放入redis
        Jedis jedis = redisUtil.getJedis();
        //使用rrdis必须明确 哪种数据类型一级key  1的意思是 使用一次 加一次
        // 可以使用redis 客户端--测测  zincrby hot 1 skuId:1 返回执行的结果
        String hotKey="hotScore";
        //int timesToEs=10;
        Double hotScore = jedis.zincrby(hotKey, 1, "skuId:" + skuId);
        //符合规则 放入es
        if(hotScore%10==0){
            //四舍五入 -11.5 是11
            updateHotScore(skuId,Math.round(hotScore));
        }
    }
    //当值 到达一定数量时  更新redis
    private void updateHotScore(String skuId, long hotScore) {
     /*
     定义dsl语句
     定义执行的动作
     执行动作
     返回结果
     */
     String updateJson="{\n" +
             "\"doc\": {\n" +
             "\"hotScore\":"+hotScore+"\n" +
             "}\n" +
             "}";
     Update update=new Update.Builder(updateJson).index(ES_INDEX).type(ES_TYPE).id(skuId).build();

        try {
            jestClient.execute(update);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //获取返回结果集-------------------------
    private SkuLsResult makeResultForSearch(SearchResult searchResult, SkuLsParams skuLsParams) {
        SkuLsResult skuLsResult = new SkuLsResult();

//        //所有商品对象
//        List<SkuLsInfo> skuLsInfoList;
        ArrayList<SkuLsInfo> skuLsInfoArrayList = new ArrayList<>();
        //skuLsInfoArrayList 数据从es中查询得到SkuLsResult
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);
        for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
            //获取skuLsInfo
            SkuLsInfo skuLsInfo = hit.source;
            //skuLsInfo中的skuName并不是高亮，所以应该获取highlight 高亮部分
            if(hit.highlight!=null && hit.highlight.size()>0){
                //获取高亮字段
                List<String>list=hit.highlight.get("skuName");
                String skuNameHI=list.get(0);
                //将原来的skuInfO中的skuName替换
                skuLsInfo.setSkuName(skuNameHI);
            }
            skuLsInfoArrayList.add(skuLsInfo);
        }
            skuLsResult.setSkuLsInfoList(skuLsInfoArrayList);
//        long total; //条数
        skuLsResult.setTotal(searchResult.getTotal());
//        long totalPages; //页数
        //也是一种写法
        // long tp=searchResult.getTotal()%skuLsParams.getPageSize()==0?searchResult.getTotal()/skuLsParams.getPageSize():searchResult.getTotal()/skuLsParams.getPageSize()+1;
     // 实际开发中用
      long totalPages=(searchResult.getTotal()+skuLsParams.getPageSize()-1)/skuLsParams.getPageSize();

        skuLsResult.setTotalPages(totalPages);
//        //平台属性值id集合
//        List<String> attrValueIdList;
        //声明一个集合来存储平台属性
        ArrayList<String> arrayList = new ArrayList<>();
        //获取平台属性值id 在聚合中获取
        //TermsAggregation 获取 平台属性值id
        TermsAggregation groupby_attr = searchResult.getAggregations().getTermsAggregation("groupby_attr");
        List<TermsAggregation.Entry> buckets = groupby_attr.getBuckets();
        for (TermsAggregation.Entry bucket : buckets) {
            String valueId= bucket.getKey();
            //将valueId 放入集合中
            arrayList.add(valueId);
        }
        skuLsResult.setAttrValueIdList(arrayList);
        return skuLsResult;
    }


    // 动态生成 del 语句----------------------
    private String makeQueryStringForSearch(SkuLsParams skuLsParams) {
        //创建查询build
        SearchSourceBuilder searchSourceBuilder=new SearchSourceBuilder();
        //bool
        BoolQueryBuilder boolQueryBuilder=new BoolQueryBuilder();

        //query
        searchSourceBuilder.query(boolQueryBuilder);
        //skuLsParams用户输入

        //判断keyworld==skuName
        if (skuLsParams.getKeyword()!=null && skuLsParams.getKeyword().length()>0) {
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", skuLsParams.getKeyword());
            boolQueryBuilder.must(matchQueryBuilder);

            //设置高亮
            // 获取高亮对象
            HighlightBuilder highlighter = searchSourceBuilder.highlighter();
            //设置高亮属性
            highlighter.postTags("</span>");
            highlighter.preTags("<span style=color:red>");
            highlighter.field("skuName");
            //将设置好的高亮对象放入查询器
            searchSourceBuilder.highlight(highlighter);
        }
        //平台属性值id
        if(skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){
            //循环遍历
            for (String valueId : skuLsParams.getValueId()) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", valueId);
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }

        //判断三级分类id
        if(skuLsParams.getCatalog3Id()!=null && skuLsParams.getCatalog3Id().length()>0){
            //获取到term
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", skuLsParams.getCatalog3Id());
            //filter----term
            boolQueryBuilder.filter(termQueryBuilder);
        }
        //设置排序
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);

        //设置分页
        //设置第几条数据开始
        //select * from skuInfo limit(pafeNum-1)*pageSize,pageSize
        int from=(skuLsParams.getPageNo()-1)*skuLsParams.getPageSize();
        searchSourceBuilder.from(from);
        //设置分为第几页
        searchSourceBuilder.size(skuLsParams.getPageSize());

        //聚合

        //term 封装聚合 agg，安装skuAttrValueList.valueId 进行聚合
        TermsBuilder groupby_attr = AggregationBuilders.terms("groupby_attr").field("skuAttrValueList.valueId");
        searchSourceBuilder.aggregation(groupby_attr);
        String query = searchSourceBuilder.toString();
        //动态生成dsl语句
        System.out.println("query"+query);
        return query;
    }

}
