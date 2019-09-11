package com.guigu.gmall.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

@Data
@NoArgsConstructor
//商品图片实体类
public class SpuImage  implements Serializable {
    @Column
    @Id
    private String id;
    @Column
    private String spuId;
    @Column
    private String imgName;
    @Column
    private String imgUrl;

}
