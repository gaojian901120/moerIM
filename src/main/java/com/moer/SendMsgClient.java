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
        System.out.println(System.currentTimeMillis());

        for (int i = 0; i < 10000; i++) {
            imMessage.setContent("I am message " + i);
            imMessage.setMsgType(2);
            imMessage.setRecv(100500 + i % 10);
            imMessage.setSend(100809070 + i % 2);
            imMessage.setSendTime(System.currentTimeMillis());
            redisStore.pubishMessage(Constant.MSG_RECV_QUEUE, JSON.toJSONString(imMessage));

        }
        System.out.println(System.currentTimeMillis());

    }
}
