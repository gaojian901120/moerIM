package com.moer;

import com.moer.business.L2ActionHandler;
import com.moer.common.HttpChannelInitializer;
import com.moer.config.NettyConfig;
import com.moer.handler.ServiceRequestHandler;
import io.netty.channel.Channel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by gaoxuejian on 2018/5/1.
 * 初始化启动时候的Channel Handler
 */
public class L2ChannelInitializer extends HttpChannelInitializer {

    public static final String PACKET_HANDLER = "packetHandler";

    private static final Logger log = LoggerFactory.getLogger(L2ChannelInitializer.class);

    private NettyConfig nettyConfig;
    private PushMessageServer application;

    public L2ChannelInitializer(NettyConfig nettyConfig, PushMessageServer application) throws Exception{
        this.nettyConfig = nettyConfig;
        this.application = application;
    }

    protected void initChannel(Channel channel) throws Exception {
        super.initChannel(channel);
        channel.pipeline().addLast(new IdleStateHandler(60,0,0, TimeUnit.SECONDS));
        channel.pipeline().addLast(PACKET_HANDLER, new ServiceRequestHandler(new L2ActionHandler()));
//        if (nettyConfig.isUseHttpCompress()) { //是否启用http压缩 gzip等
//            pipeline.addLast(HTTP_COMPRESSION, new HttpContentCompressor()); //outbound
//        }

//        pipeline.addLast(PACKET_HANDLER, packetHandler); //in bound  包处理handler 从PacketMessage里面解码出client信息，传输协议，正文packet
//
//        pipeline.addLast(AUTHORIZE_HANDLER, authorizeHandler);//inbound packet解析后 需要进行认证
//        pipeline.addLast(XHR_POLLING_TRANSPORT, xhrPollingTransport);//inbound 如果请求是polling 则使用这个handler进行处理
//
//
//        pipeline.addLast(SOCKETIO_ENCODER, encoderHandler); //outboun

    }
}
