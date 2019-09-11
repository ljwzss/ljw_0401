package com.guigu.gmall.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

@Data
@NoArgsConstructor
//一级分类列表
public class BaseCatalog1 implements Serializable {
    @Id //主键唯一id
    @Column //代表 跟数据库字段一样
    private String id;
    @Column
    private String name;
}
