package com.guigu.gmall.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;



import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

@Data
@NoArgsConstructor
//加载销售属性--常用
public class BaseSaleAttr implements Serializable{
    @Id
    @Column
    String id ;

    @Column
    String name;
}
