package com.moer;

import com.moer.handler.RedisMessageHandler;
import com.moer.server.PushMessageServer;
import com.moer.store.RedisStore;
import io.netty.util.concurrent.Future;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

/**
 * Created by gaoxuejian on 2018/5/1.
 * 使用Springboot 基本配置 该入口类比如放置在包的最顶层，这样会扫描该包及其下面的所有的子包，也就是同层次的包不会扫描到，也不会autowire自动加载出来
 */
@SpringBootApplication
public class ImServerApplication implements CommandLineRunner {
    @Autowired
    private PushMessageServer nettyServer;
    @Autowired
    private RedisStore redisStore;

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(ImServerApplication.class, args);
    }

    public void run(String... strings) throws Exception {
        Future future = nettyServer.start();
        nettyServer.initData();

        RedisMessageHandler messageListener = new RedisMessageHandler(nettyServer);
        redisStore.subscribeChannel(Constant.ROOT_QUEUE, messageListener);

        future.sync();
    }


}
