package com.moer.thread;

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
        while (true){
            try {
                redisStore.subscribeChannel(messageListener, Constant.MSG_RECV_QUEUE, Constant.DATA_SYNC_QUEUE);
            }catch (Exception e){
                messageListener.unsubscribe( Constant.MSG_RECV_QUEUE, Constant.DATA_SYNC_QUEUE);
                redisStore.subscribeChannel(messageListener, Constant.MSG_RECV_QUEUE, Constant.DATA_SYNC_QUEUE);
            }
        }
    }
}
