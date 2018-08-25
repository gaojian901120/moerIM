package com.moer;

import com.alibaba.fastjson.JSON;
import com.moer.common.Constant;
import com.moer.entity.ImMessage;
import com.moer.redis.RedisStore;
import com.moer.util.ConfigUtil;

/**
 * Created by gaoxuejian on 2018/5/2.
 */
public class SendMsgClient {

    public static void main(String[] args) {
        RedisStore redisStore = new RedisStore(ConfigUtil.loadRedisConfig());
        ImMessage imMessage = new ImMessage();
        for (int i = 0; i < 100; i++) {
            imMessage.setMsg("I am message " + i);
            imMessage.setMsgType(2);
            imMessage.setRecv("27711241256961");
            imMessage.setSend(String.valueOf(100809070 + i % 2));
            imMessage.setSendTime(System.nanoTime());
            redisStore.pubishMessage(Constant.MSG_RECV_QUEUE, JSON.toJSONString(imMessage));
        }
        System.out.println(System.currentTimeMillis());
    }
}
