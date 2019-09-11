package com.guigu.thymelef.thymelef.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;

@Controller
public class IndexController {

    @RequestMapping("index")
    public String index(HttpServletRequest request, Model model, HttpSession session) {
        //存储数剧
        request.setAttribute("tName", "你好");

        //存储集合
        ArrayList<String>list=new ArrayList<>();
        list.add("林俊杰");
        list.add("张靓颖");
        list.add("陈赫");
        list.add("吴京");

        request.setAttribute("lists", list);

        model.addAttribute("age",18);

        session.setAttribute("sex","boy");
        //th:utext :解析样式
        request.setAttribute("green","<span style='color:green'>男孩</span>");
        return  "index";
    }
}