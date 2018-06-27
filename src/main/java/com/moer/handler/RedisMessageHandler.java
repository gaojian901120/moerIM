package com.moer.handler;

import com.alibaba.fastjson.JSON;
import com.moer.common.Constant;
import com.moer.entity.ImMessage;
import com.moer.server.DispatchServer;
import com.moer.server.PushMessageServer;
import redis.clients.jedis.JedisPubSub;

import java.util.Map;

/**
 * Created by gaoxuejian on 2018/5/3.
 */
public class RedisMessageHandler extends JedisPubSub {

    public RedisMessageHandler() {
    }

    @Override
    public void onMessage(String channel, String message) {
        if (channel == Constant.MSG_RECV_QUEUE) {
            ImMessage imMessage = JSON.parseObject(message, ImMessage.class);
            MessageDispatchHandler handler = new MessageDispatchHandler(1, imMessage);
            DispatchServer.dispatchMsg(handler);
        }else if (channel == Constant.DATA_SYNC_QUEUE) {
            Map<String,Object> action = (Map<String, Object>) JSON.parse(message);
            String tableName = ()action.get("tableName");
        }
    }
}
