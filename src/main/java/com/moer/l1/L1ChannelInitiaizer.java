package com.moer.l1;

import com.moer.common.HttpChannelInitializer;
import com.moer.handler.RequestHandler;
import com.moer.l1.business.L1ActionHandler;
import io.netty.channel.Channel;

/**
 * Created by gaoxuejian on 2018/5/18.
 * L1 连接层的节点的请求处理handler
 */
public class L1ChannelInitiaizer extends HttpChannelInitializer {
    public static String L1_REQUEST_HANDLER = "L1RequestHadnler";

    @Override
    protected void initChannel(Channel channel) throws Exception {
        super.initChannel(channel);
        channel.pipeline().addLast(L1_REQUEST_HANDLER, new RequestHandler(new L1ActionHandler()));
    }
}
