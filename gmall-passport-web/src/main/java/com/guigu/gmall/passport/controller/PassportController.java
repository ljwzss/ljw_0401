package com.guigu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.guigu.gmall.bean.UserInfo;
import com.guigu.gmall.passport.util.JwtUtil;
import com.guigu.gmall.service.UserService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@CrossOrigin
public class PassportController {

    @Reference
   private UserService userService;

    @Value("${token.key}")
    String key;

    //登录页面展示
    @RequestMapping("index")
    public  String index(HttpServletRequest request){
        //存储一个originUrl
        //用户点击登录的时候，必须从某个页面链接访问登录模块，则登录url后面必须有你点击的那个链接
        String originUrl = request.getParameter("originUrl");
        request.setAttribute("originUrl",originUrl);
        return "index";
    }
    //实现登录
    @RequestMapping("login")
    @ResponseBody
    public  String login(UserInfo userInfo,HttpServletRequest request){
      UserInfo info=  userService.login(userInfo);
      if(info!=null){
          //生成token  key  map salt
          //服务器IP地址 在服务器中设置X-forwarded-for 对应的值
          String salt = request.getHeader("X-forwarded-for");
          HashMap<String, Object> map = new HashMap<>();
          map.put("userId",info.getId());
          map.put("nickName",info.getNickName());
          String token = JwtUtil.encode(key, map, salt);
          return token;
      }else {
          return "fail";
      }
    }

    //认证方式
    //http://passport.guigu.com/verify?token=xxx&salt=xxx
    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request){
        //上边已经有key token salt
        String token=request.getParameter("token");
        String salt=request.getParameter("salt");

        //解密
        Map<String, Object> map = JwtUtil.decode(token, key, salt);
        if(map!=null && map.size()>0){
            //获取用户id
            String userId = (String) map.get("userId");
            //调用认证方法
            UserInfo userInfo=userService.verify(userId);
            if(userInfo!=null){
                return "success";
            }
        }
        return "fail";
    }
}
