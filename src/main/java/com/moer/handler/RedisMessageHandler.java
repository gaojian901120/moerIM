package com.moer.handler;

import com.alibaba.fastjson.JSON;
import com.moer.entity.ImMessage;
import com.moer.server.DispatchServer;
import com.moer.server.PushMessageServer;
import redis.clients.jedis.JedisPubSub;

/**
 * Created by gaoxuejian on 2018/5/3.
 */
public class RedisMessageHandler extends JedisPubSub {

    public RedisMessageHandler() {
    }

    @Override
    public void onMessage(String channel, String message) {
        ImMessage imMessage = JSON.parseObject(message, ImMessage.class);
        MessageDispatchHandler handler = new MessageDispatchHandler(1, imMessage);
        DispatchServer.dispatchMsg(handler);
    }
}
