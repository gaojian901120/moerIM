package com.moer.l2;

import com.moer.common.Constant;
import com.moer.common.ServiceFactory;
import com.moer.config.NettyConfig;
import com.moer.handler.RedisMessageHandler;
import com.moer.redis.RedisConfig;
import com.moer.redis.RedisStore;
import com.moer.util.ConfigUtil;
import com.moer.zookeeper.NodeManager;
import com.moer.zookeeper.ZkConfig;
import io.netty.util.concurrent.Future;

import java.io.BufferedReader;
import java.io.InputStreamReader;


public class L2ServiceApplication {
    public static void main(String[] args) throws Exception {
        //读取配置信息
        String host = "im.moer.cn";
        System.out.print("Please input the port:");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int port = Integer.valueOf(br.readLine());
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

        L2ApplicationContext.getInstance().timerThread.start();
        L2ApplicationContext.getInstance().dataSyncToRedisThread.start();
        PushMessageServer nettyServer = new PushMessageServer(nettyConfig);
        Future future = nettyServer.start();
        nettyServer.initData();
        NodeManager nodeManager = NodeManager.getInstance();
        nodeManager.setConfig(zkConfig, nettyConfig, "l2");
        new Thread(nodeManager).start();
        RedisMessageHandler messageListener = new RedisMessageHandler();
        RedisStore redisStore = ServiceFactory.getRedis();
        redisStore.subscribeChannel(messageListener,Constant.MSG_RECV_QUEUE, Constant.DATA_SYNC_QUEUE);
    }


}
