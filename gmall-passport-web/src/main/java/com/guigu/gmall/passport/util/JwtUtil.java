package com.guigu.gmall.passport.util;

import io.jsonwebtoken.*;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

public class JwtUtil {
        /*
        key 公共部分
        param  用户信息
        salt  服务器IP

        */
    //生成 token
    public static String encode(String key, Map<String, Object> param, String salt) {
        if (salt != null) {
            //私有部分
            key += salt;
        }
        JwtBuilder jwtBuilder = Jwts.builder().signWith(SignatureAlgorithm.HS256, key);
        //用户信息放入token中
        jwtBuilder = jwtBuilder.setClaims(param);

        String token = jwtBuilder.compact();
        return token;

    }

            /*
        key 公共部分
        token  字符串
        salt  私有部分

        */
        //反解密
    public static Map<String, Object> decode(String token, String key, String salt) {
        Claims claims = null;
        if (salt != null) {
            key += salt;
        }
        try {
            claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        } catch (JwtException e) {
            return null;
        }
// 解密出用户信息
        return claims;
    }
}

