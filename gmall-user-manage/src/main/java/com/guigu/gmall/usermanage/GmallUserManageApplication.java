package com.guigu.gmall.usermanage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

//提供者
@SpringBootApplication
@MapperScan(basePackages = "com.guigu.gmall.usermanage.mapper") //注意导包导对
@ComponentScan("com.guigu.gmall")
public class GmallUserManageApplication {

	public static void main(String[] args) {

		SpringApplication.run(GmallUserManageApplication.class, args);
	}

}
