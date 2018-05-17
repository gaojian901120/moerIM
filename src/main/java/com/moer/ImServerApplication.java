package com.moer;

import com.moer.config.NettyConfig;
import com.moer.handler.RedisMessageHandler;
import com.moer.redis.RedisConfig;
import com.moer.redis.RedisStore;
import com.moer.server.PushMessageServer;
import com.moer.util.ConfigUtil;
import com.moer.zookeeper.NodeManager;
import com.moer.zookeeper.ZkConfig;
import io.netty.util.concurrent.Future;

/**
 * Created by gaoxuejian on 2018/5/1.
 * 使用Springboot 基本配置 该入口类比如放置在包的最顶层，这样会扫描该包及其下面的所有的子包，也就是同层次的包不会扫描到，也不会autowire自动加载出来
 */
public class ImServerApplication {
    public static void main(String[] args) throws Exception {
        //读取配置信息
        NettyConfig nettyConfig = ConfigUtil.loadNettyConfig();
        RedisConfig redisConfig = ConfigUtil.loadRedisConfig();
        ZkConfig zkConfig = ConfigUtil.loadZkConfig();

        NodeManager nodeManager = new NodeManager(zkConfig);
        if (!nodeManager.createChildNode(nettyConfig.getHostName(), String.valueOf(nettyConfig.getPort()))) return;

        PushMessageServer nettyServer = new PushMessageServer(nettyConfig);
        Future future = nettyServer.start();
        nettyServer.initData();

        RedisMessageHandler messageListener = new RedisMessageHandler(nettyServer);
        RedisStore redisStore = new RedisStore(redisConfig);
        redisStore.subscribeChannel(Constant.MSG_RECV_QUEUE, messageListener);
        future.sync();
    }


}
