package com.guigu.gmall.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
//属性
public class BaseAttrInfo implements Serializable {
    @Id
    @Column
    //属性添加
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    @Column
    private String attrName;
    @Column
    private String catalog3Id;

    //属性的保存--------
    @Transient  //不保存到数据库
    // 属性里有多个属性值
    private List<BaseAttrValue> attrValueList;

    //
}
