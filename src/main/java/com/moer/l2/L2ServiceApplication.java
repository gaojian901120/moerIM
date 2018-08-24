package com.moer.l2;

import com.moer.common.Constant;
import com.moer.config.NettyConfig;
import com.moer.handler.RedisMessageHandler;
import com.moer.redis.RedisConfig;
import com.moer.redis.RedisStore;
import com.moer.server.PushMessageServer;
import com.moer.service.ServiceFactory;
import com.moer.util.ConfigUtil;
import com.moer.zookeeper.NodeManager;
import com.moer.zookeeper.ZkConfig;
import io.netty.util.concurrent.Future;


public class L2ServiceApplication {
    public static void main(String[] args) throws Exception {
        //读取配置信息
        String host = "127.0.0.1";
        int port = 9002;
        NettyConfig nettyConfig = ConfigUtil.loadNettyConfig();
        nettyConfig.setHostName(host);
        nettyConfig.setPort(port);
        RedisConfig redisConfig = ConfigUtil.loadRedisConfig();
        if(!ServiceFactory.init(redisConfig)){
            return;
        }
        ZkConfig zkConfig = ConfigUtil.loadZkConfig();
        L2ApplicationContext.getInstance().imConfig = ConfigUtil.loadImConfig();
        L2ApplicationContext.getInstance().nettyConfig = nettyConfig;
        NodeManager nodeManager = NodeManager.getInstance();
        nodeManager.setConfig(zkConfig, nettyConfig, "l2");
        new Thread(nodeManager).start();
        L2ApplicationContext.getInstance().timerThread.start();
        PushMessageServer nettyServer = new PushMessageServer(nettyConfig);
        Future future = nettyServer.start();
        nettyServer.initData();

        RedisMessageHandler messageListener = new RedisMessageHandler();
        RedisStore redisStore = ServiceFactory.getRedis();
        redisStore.subscribeChannel(Constant.MSG_RECV_QUEUE, messageListener);
        redisStore.subscribeChannel(Constant.DATA_SYNC_QUEUE, messageListener);
        future.sync();
    }


}
