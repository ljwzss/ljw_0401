package com.guigu.gmall.config;



import com.alibaba.fastjson.JSON;
import com.guigu.gmall.util.HttpClientUtil;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Map;
//将将token放入cookie中
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    //进入控制器之前
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //http://item.gmall.com/50.html?newToken=eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkF0Z3VpZ3UiLCJ1c2VySWQiOiIxIn0.SDg-KEQupVShkVoBkfRmuIsLJ1UhpFxZVuP8OrRRMfo
        // 登录成功后回去newToken
        String token = request.getParameter("newToken");
        //当token不为空的时候，将token放入cookie中
        if(token!=null){
            CookieUtil.setCookie(request,response,"token",token,WebConst.COOKIE_MAXAGE,false);
        }
        //http://item.gmall.com/46.html 当用户访问其他业务模块的时候，没有newToken，但是cookie中有可能存在token
        // token为空的时候
        if(token==null){
            token = CookieUtil.getCookieValue(request, "token", false);
        }

        //从token中获取用户名称
        if(token!=null){
            //获取名字
            Map map=getUserMapByToken(token);
            String nickName= (String) map.get("nickName");
            //保存名字
            request.setAttribute("nickName",nickName);
        }

        //检验方法是否需要验证用户登录状态---自定义注解

        //判断当前控制器上是否有注解----------------
        HandlerMethod handlerMethod =(HandlerMethod) handler;
        //看方法上是否有注解
        LoginRequire loginRequireAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
        if(loginRequireAnnotation!=null){
        //获取到注解
        //认证：用户是否登录的认证 调用PassPortController中的verify控制器
        //   获取salt(盐)
            String salt = request.getHeader("x-forwarded-for");
            //跨域访问--把工具类添加进来
        /*http://passport.guigu.com/verify
        http://passport.guigu.com/verify?token=xxx&salt=xxx
        */
            String result = HttpClientUtil.doGet(WebConst.VERIFY_ADDRESS + "?token=" + token + "&salt=" + salt);
             //判断执行结果
            if("success".equals(result)){
                //保存userId
                Map map = getUserMapByToken(token);
                String userId =(String) map.get("userId");
                request.setAttribute("userId",userId);
                return true;
            }else{
                 //什么情况下必须登录
                if(loginRequireAnnotation.autoRedirect()){
                    //必须登录
                    //http://passport.guigu.com/index?originUrl=http%3A%2F%2Fitem.gmall.com%2F50.html
                    //获取浏览器的url  http://item.gmall.com/46.html
                    String  requestURL = request.getRequestURL().toString();
                    //是要得到转码后的路径
                    String encodeURL = URLEncoder.encode(requestURL, "UTF-8");
                    //重定向到登录界面 http://passport.guigu.com/index
                    response.sendRedirect(WebConst.LOGIN_ADDRESS+"?originUrl="+encodeURL);
                    return false;//不会走下个拦截器
                }
            }
        }

        return true;
    }

    private Map getUserMapByToken(String token) {
        //eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkF0Z3VpZ3UiLCJ1c2VySWQiOiIxIn0.SDg-KEQupVShkVoBkfRmuIsLJ1UhpFxZVuP8OrRRMfo
        //map属于第二部分 ，可以使用JWTUtil工具类，base64编码

        //获取token中第二部分的数据
        String tokenUserInfo  = StringUtils.substringBetween(token, ".");

        //创建Base64
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        //解码
        byte[] tokenBytes  = base64UrlCodec.decode(tokenUserInfo);
        String tokenJson =null;
        //编码集 转换
        try {
             tokenJson  = new String(tokenBytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Map map= JSON.parseObject(tokenJson,Map.class);
        return map;
    }

    //进入控制器之后，返回视图之前
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }
    //返回视图之后
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
