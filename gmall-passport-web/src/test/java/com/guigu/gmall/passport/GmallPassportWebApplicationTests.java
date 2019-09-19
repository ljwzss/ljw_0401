package com.guigu.gmall.passport;

import com.guigu.gmall.passport.util.JwtUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPassportWebApplicationTests {

	@Test
	public void contextLoads() {
	}

	@Test
	public void test01(){
		String key="atguigu";
		HashMap<String, Object> map = new HashMap<>();
		map.put("userId","111");
		map.put("nickName","admin");
		String salt="192.168.152.134";
		String token = JwtUtil.encode(key, map, salt);
		System.out.println("token===="+token);
		//解密
		Map<String, Object> objectMap = JwtUtil.decode(token, key, salt);
		System.out.println(objectMap.get("userId"));
		System.out.println(objectMap.get("nickName"));
	}
}
