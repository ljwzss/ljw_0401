package com.guigu.gmall.usermanage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;


@SpringBootApplication
@MapperScan(basePackages = "com.guigu.gmall.usermanage.mapper") //注意导包导对
public class GmallUserManageApplication {

	public static void main(String[] args) {

		SpringApplication.run(GmallUserManageApplication.class, args);
	}

}
