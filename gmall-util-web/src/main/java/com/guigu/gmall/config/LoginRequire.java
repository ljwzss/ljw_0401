package com.guigu.gmall.config;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) //作用到方法级别
//注解的生命周期
@Retention(RetentionPolicy.RUNTIME) //运行时生效
public @interface LoginRequire {
    boolean autoRedirect() default true; //跟你加载方法上注解对应@LoginRequire(autoRedirect = true) //代表访问商品详情页必须登录
}
