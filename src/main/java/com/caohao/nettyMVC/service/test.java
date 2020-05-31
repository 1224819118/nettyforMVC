package com.caohao.nettyMVC.service;

import com.caohao.nettyMVC.util.Message;

import java.util.Map;

public class test {
    public String test(Message message){
        Map<String, String> msg = message.msg;
        String username = (String) msg.get("username");
        String password = (String) msg.get("password");
        if (username.equals("caohao")&&password.equals("123456")){
            return "loginsuccess";
        }
        return "loginfaile";
    }
}
