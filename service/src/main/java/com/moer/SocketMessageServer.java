package com.moer;

import com.moer.config.NettyConfig;
import com.moer.socket.MessageDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class SocketMessageServer {
    private static final Logger logger = LoggerFactory.getLogger(SocketMessageServer.class);
    private static final int bossThread = 2;
    private static final int workerThread = 8;
    public void start(){
        EventLoopGroup boss = new EpollEventLoopGroup(bossThread);
        EventLoopGroup worker = new EpollEventLoopGroup(workerThread);
        Class<? extends ServerChannel> channelClass = EpollServerSocketChannel.class;
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boss,worker)
                .channel(channelClass)
                .option(ChannelOption.SO_BACKLOG, 65535)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        channel.pipeline()
                                .addLast("IdleCheck", new IdleStateHandler(60,0,0, TimeUnit.SECONDS))
                                .addLast("packageSplit",new LengthFieldBasedFrameDecoder(65535,5,2,0,0))
                                .addLast("messageDecoder",new MessageDecoder());
                    }
                });
    }
}
