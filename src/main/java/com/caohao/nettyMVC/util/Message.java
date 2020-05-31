package com.caohao.nettyMVC.util;

import java.util.HashMap;
import java.util.Map;

public class Message {
    public Map<String,String> msg = new HashMap<>();
    public void setmsg(String key,String value){
        msg.put(key,value);
    }
    public static Message getMessage(String message){
        Message result = new Message();
        String[] messages = message.split("&");
        for (String s:messages){
            String[] split = s.split("=");
            result.setmsg(split[0],split[1]);
        }
        return result;
    }
}
