package com.guigu.gmall.bean;

import lombok.Data;
import lombok.NoArgsConstructor;
import tk.mybatis.mapper.common.Mapper;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
//商品  三级展示
public class SpuInfo implements Serializable {
    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column
    private String spuName;

    @Column
    private String description;

    @Column
    private  String catalog3Id;

    //商品图片实体类
    @Transient
    private List<SpuImage>spuImageList;
    //销售属性表
    @Transient
    private List<SpuSaleAttr> spuSaleAttrList;
}
