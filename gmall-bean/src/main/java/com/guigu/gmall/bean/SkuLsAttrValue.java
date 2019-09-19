package com.guigu.gmall.bean;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class SkuLsAttrValue implements Serializable {
    String valueId;
}
