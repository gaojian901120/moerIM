package com.moer.l2.thread;

import com.moer.common.Constant;
import com.moer.common.ServiceFactory;
import com.moer.handler.RedisMessageHandler;
import com.moer.redis.RedisStore;

/**
 * Created by gaoxuejian on 2019/2/22.
 */
public class SubscribeThread extends Thread{
    @Override
    public void run() {
        Thread.currentThread().setName("SubscribeThread");
        RedisMessageHandler messageListener = new RedisMessageHandler();
        RedisStore redisStore = ServiceFactory.getRedis();
        redisStore.subscribeChannel(messageListener, Constant.MSG_RECV_QUEUE, Constant.DATA_SYNC_QUEUE);
    }
}
