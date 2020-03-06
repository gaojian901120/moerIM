package com.moer;

import com.moer.common.ServiceFactory;
import com.moer.config.NettyConfig;
import com.moer.redis.RedisConfig;
import com.moer.util.ConfigUtil;
import io.netty.util.concurrent.Future;
import sun.misc.Signal;
import sun.misc.SignalHandler;

public class L2ServiceApplication {

    public static void main(String[] args) throws Exception {
        int processors = Runtime.getRuntime().availableProcessors();
        //读取配置信息
        String host = String.valueOf(args[0]);
        NettyConfig nettyConfig = ConfigUtil.loadNettyConfig();
        nettyConfig.setHostName(host);
        nettyConfig.setPort(443);//使用默认ssl端口 443
        RedisConfig redisConfig = ConfigUtil.loadRedisConfig();
        if(!ServiceFactory.init(redisConfig)){
            return;
        }
        PushMessageServer nettyServer = new PushMessageServer(nettyConfig);
        Future future = nettyServer.start();

        com.moer.L2ApplicationContext.getInstance().monitorThread.start();


        future.syncUninterruptibly();
    }
    public static class SingleHandler implements SignalHandler {
        public void handle(Signal signal) {
//            System.out.println(signal.getName() + "is recevied.");
//            String name = signal.getName();
//            if(name.equals("USR2")){//信号1 读取所有im.Properties的配置
//                L2ApplicationContext.getInstance().imConfig = ConfigUtil.loadImConfig();
//            }
        }
    }

}
