package com.moer.l1;

import com.moer.config.NettyConfig;
import com.moer.util.ConfigUtil;
import com.moer.zookeeper.NodeManager;
import com.moer.zookeeper.ZkConfig;
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
 * Created by gaoxuejian on 2018/5/15.
 * 入口服务:用户首次进入连接该服务器获取提供服务的节点地址
 * 程序启动顺序
 * 1、启动服务层节点，节点启动后，将节点注册到zookeeper，
 * 2、服务节点全部启动完毕后，启动连接层节点，连接层节点获取zookeeper已经注册的服务层节点信息，生成转发规则，同时告诉服务层节点可以初始化数据了。
 * 3、服务层节点收到初始化数据的信息后，进行数据初始化并打开服务端口，开始提供服务。然后告诉连接层节点 可以对外提供服务了。
 * 4、连接层节点收到服务已经启动的信息后，打开端口，正式对外提供服务。
 * 以上不同节点之间的交互都是通过zookeeper进行交互的。
 * 连接层节点的状态：1、imregiste（服务节点注册到zk完成），3、ready(业务数据初始化完成) 5、imruleupdate(新增删除节点 路由表更新后)
 * 服务层节点的状态：2、entrtyconnect(入口节点注册到zk完成) 4、work（正式提供服务） 6、workupdate（）
 */
public class L1EntryApplication {
    public static final Logger logger = LoggerFactory.getLogger(L1EntryApplication.class);

    public static void main(String[] args) {
        //连接zookeeper
        ZkConfig zkConfig = ConfigUtil.loadZkConfig();
        try {
            NodeManager nodeManager = NodeManager.getInstance();
            if (!nodeManager.init(zkConfig)) {
                return;
            }
            if (!nodeManager.createRootNode())
                return;
//            if (!nodeManager.checkAndMonitorChildStat()) {
//                return;
//            }
            //启动netty 服务 开始对外提供服务
            NettyConfig nettyConfig = ConfigUtil.loadNettyConfig();
            EventLoopGroup boss = null;
            EventLoopGroup worker = null;
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            L1ChannelInitiaizer l1ChannelInitializer = new L1ChannelInitiaizer();
            if (nettyConfig.isUseEpoll()) {
                boss = new EpollEventLoopGroup();
                worker = new EpollEventLoopGroup();
            } else {
                boss = new NioEventLoopGroup();
                worker = new NioEventLoopGroup();
            }
            Class<? extends ServerChannel> channelClass = NioServerSocketChannel.class;
            if (nettyConfig.isUseEpoll()) {
                channelClass = EpollServerSocketChannel.class;
            }
            serverBootstrap
                    .group(boss, worker)
                    .channel(channelClass)
                    .childHandler(l1ChannelInitializer);
            InetSocketAddress addr = new InetSocketAddress(nettyConfig.getPort());
            if (nettyConfig.getHostName() != null) {
                addr = new InetSocketAddress(nettyConfig.getHostName(), nettyConfig.getPort());
            }

            ChannelFuture future = serverBootstrap.bind(addr).addListener(new FutureListener<Void>() {
                public void operationComplete(Future<Void> future) throws Exception {
                    if (future.isSuccess()) {
                        logger.info("Moer IM L1 server started at host: {},port: {}", nettyConfig.getHostName(), nettyConfig.getPort());
                    } else {
                        logger.error("Moer IM L1 server start failed at hsot: {},  port: {}!", nettyConfig.getHostName(), nettyConfig.getPort());
                    }
                }
            }).syncUninterruptibly();
        } catch (Exception e) {
            logger.error("server start error, message: " + e.getMessage());
        }
    }

}
