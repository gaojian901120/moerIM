package com.moer;

import com.moer.business.L1ActionHandler;
import com.moer.common.HttpChannelInitializer;
import com.moer.handler.DispatchRequestHandler;
import io.netty.channel.Channel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * Created by gaoxuejian on 2018/5/18.
 * L1 连接层的节点的请求处理handler
 */
public class L1ChannelInitiaizer extends HttpChannelInitializer {
    public static String L1_REQUEST_HANDLER = "L1RequestHadnler";
    public L1ChannelInitiaizer() throws Exception {
    }
    @Override
    protected void initChannel(Channel channel) throws Exception {
        super.initChannel(channel);
        channel.pipeline().addLast(new IdleStateHandler(10,0,0, TimeUnit.SECONDS));
        channel.pipeline().addLast(L1_REQUEST_HANDLER, new DispatchRequestHandler(new L1ActionHandler()));
    }
}
