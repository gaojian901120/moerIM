package com.moer.l2;

import com.moer.config.NettyConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
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

    public PushMessageServer(NettyConfig config) {
        nettyConfig = new NettyConfig(config);
        serverBootstrap = new ServerBootstrap();
        imChannelInitializer = new L2ChannelInitializer(nettyConfig, this);
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
                .childHandler(imChannelInitializer).option(ChannelOption.SO_BACKLOG,65535);
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
        }).syncUninterruptibly();
        return future;
    }

    public void initData() {
        //测试模拟一个摩尔大秘书的直播 21264559308801  101000002  订阅人数不到八万 模拟极限在线情况 @TODO 不好模拟 先测简单的
        //16559074508801  向这个直播间发消息  102657004 来接受消息
//        GroupMembersService membersService = ServiceFactory.getInstace(GroupMembersService.class);
//        GroupMembers members = new GroupMembers();
//        members.setUid(102657004);
//        List<GroupMembers> membersList = membersService.getMember(members);
    }
}
