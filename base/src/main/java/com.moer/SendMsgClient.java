package com.moer;

import com.alibaba.fastjson.JSON;
import com.moer.common.Constant;
import com.moer.entity.ImMessage;
import com.moer.redis.RedisStore;
import com.moer.util.ConfigUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaoxuejian on 2018/5/2.
 */
public class SendMsgClient {
    public  static int reverseBits( int n) {
        int c = 0;
        int b = 0x01;
        while(n >0){
            int l = n & b;
            if (l==1) c++;
            n= n >> 1;
        }
        return c;
    }
    public static void main(String[] args) {
        RedisStore redisStore = new RedisStore(ConfigUtil.loadRedisConfig());
        ImMessage imMessage = new ImMessage();
        for (int i = 0; i < 10; i++) {
            imMessage.setMsg("I am message " + i);
            imMessage.setMsgType(1);
            imMessage.setChatType(1);
            imMessage.setShowType(1);
            imMessage.setRecv("100");
            imMessage.setSend(String.valueOf(100809071 + i % 2));
            imMessage.setSendTime(System.nanoTime());
            Map<String,Object> ext = new HashMap<>();
            ext.put("priority",i%10);
            imMessage.setExtp(JSON.toJSONString(ext));
            redisStore.pubishMessage(Constant.MSG_RECV_QUEUE, JSON.toJSONString(imMessage));
        }
        System.out.println(System.currentTimeMillis());
    }
}
