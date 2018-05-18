package com.moer.server;

import com.moer.L2ChannelInitializer;
import com.moer.config.NettyConfig;
import com.moer.store.GroupStore;
import com.moer.store.UserStore;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Created by gaoxuejian on 2018/5/1.
 * 一个accept进程组 一个io进程组
 * 负责向客户端推送消息
 */
public class PushMessageServer {
    private static final Logger log = LoggerFactory.getLogger(PushMessageServer.class);
    private final NettyConfig nettyConfig;
    private ServerBootstrap serverBootstrap;
    private L2ChannelInitializer imChannelInitializer;
    private EventLoopGroup boss;
    private EventLoopGroup worker;
    private UserStore userStore;
    private GroupStore groupStore;

    public PushMessageServer(NettyConfig config) {
        nettyConfig = new NettyConfig(config);
        serverBootstrap = new ServerBootstrap();
        imChannelInitializer = new L2ChannelInitializer(nettyConfig, this);
        userStore = new UserStore();
        groupStore = new GroupStore();
    }

    public Future start() {
        int port = nettyConfig.getPort();
        boolean useEpoll = nettyConfig.isUseEpoll();
        String hostName = nettyConfig.getHostName();
        if (useEpoll) {
            boss = new EpollEventLoopGroup();
            worker = new EpollEventLoopGroup();
        } else {
            boss = new NioEventLoopGroup();
            worker = new NioEventLoopGroup();
        }
        Class<? extends ServerChannel> channelClass = NioServerSocketChannel.class;
        if (useEpoll) {
            channelClass = EpollServerSocketChannel.class;
        }
        serverBootstrap
                .group(boss, worker)
                .channel(channelClass)
                .childHandler(imChannelInitializer);
        InetSocketAddress addr = new InetSocketAddress(port);
        if (hostName != null) {
            addr = new InetSocketAddress(hostName, port);
        }

        ChannelFuture future = serverBootstrap.bind(addr).addListener(new FutureListener<Void>() {
            public void operationComplete(Future<Void> future) throws Exception {
                if (future.isSuccess()) {
                    log.info("Moer IM server started at port: {}", nettyConfig.getPort());
                } else {
                    log.error("Moer IM server start failed at port: {}!", nettyConfig.getPort());
                }
            }
        });
        return future;
    }

    public void initData() {
        userStore.addOnlineUser(100809070);
        groupStore.addGroupUser(100500, 100809070);
        userStore.addOnlineUser(100809071);
        groupStore.addGroupUser(100500, 100809071);
        userStore.addOnlineUser(100809072);
        groupStore.addGroupUser(100500, 100809072);
        userStore.addOnlineUser(100809073);
        groupStore.addGroupUser(100500, 100809073);
        userStore.addOnlineUser(100809074);
        groupStore.addGroupUser(100500, 100809074);

        userStore.addOnlineUser(100809070);
        groupStore.addGroupUser(100503, 100809070);
        userStore.addOnlineUser(100809071);
        groupStore.addGroupUser(100503, 100809071);
        userStore.addOnlineUser(100809072);
        groupStore.addGroupUser(100503, 100809072);
        userStore.addOnlineUser(100809073);
        groupStore.addGroupUser(100503, 100809073);
        userStore.addOnlineUser(100809074);
        groupStore.addGroupUser(100503, 100809074);

    }

    public GroupStore getGroupStore() {
        return groupStore;
    }


    public UserStore getUserStore() {
        return userStore;
    }

}
