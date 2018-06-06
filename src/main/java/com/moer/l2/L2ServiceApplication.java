package com.moer.l2;

import com.moer.common.Constant;
import com.moer.config.NettyConfig;
import com.moer.handler.RedisMessageHandler;
import com.moer.redis.RedisConfig;
import com.moer.redis.RedisStore;
import com.moer.server.PushMessageServer;
import com.moer.util.ConfigUtil;
import com.moer.zookeeper.NodeManager;
import com.moer.zookeeper.ZkConfig;
import io.netty.util.concurrent.Future;


public class L2ServiceApplication {
    public static void main(String[] args) throws Exception {
        //读取配置信息
        String host = "127.0.0.1";
        int port = 9001;
        NettyConfig nettyConfig = ConfigUtil.loadNettyConfig();
        nettyConfig.setHostName(host);
        nettyConfig.setPort(port);
        RedisConfig redisConfig = ConfigUtil.loadRedisConfig();
        ZkConfig zkConfig = ConfigUtil.loadZkConfig();
        L2ApplicationContext.getInstance().imConfig = ConfigUtil.loadImConfig();
        NodeManager nodeManager = NodeManager.getInstance();
        nodeManager.init(zkConfig, nettyConfig);
        if (!nodeManager.createRootNode())
            return;
        if (!nodeManager.createNode("child", nettyConfig.getHostName(), String.valueOf(nettyConfig.getPort())))
            return;
        if (!nodeManager.checkAndMonitorChildStat())
            return;
        PushMessageServer nettyServer = new PushMessageServer(nettyConfig);
        Future future = nettyServer.start();
        nettyServer.initData();

        RedisMessageHandler messageListener = new RedisMessageHandler(nettyServer);
        RedisStore redisStore = new RedisStore(redisConfig);
        redisStore.subscribeChannel(Constant.MSG_RECV_QUEUE, messageListener);
        future.sync();
    }


}
