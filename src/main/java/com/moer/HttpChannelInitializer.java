package com.moer;

import com.moer.handler.PacketHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import static com.moer.L2ChannelInitializer.HTTP_AGGREGATOR;

/**
 * Created by gaoxuejian on 2018/5/18.
 */
public class HttpChannelInitializer extends ChannelInitializer<Channel> {

    /**
     * inbound http解码器
     */
    public static final String HTTP_REQUEST_DECODER = "httpDecoder";
    /**
     * inbound http聚合器 用于处理大包的http 请求响应
     */
    public static final String HTTP_AGGREGATOR = "httpAggregator";
    /**
     * inbound 包处理器 用于解析业务上收到的IM 消息
     */
    public static final String PACKET_HANDLER = "packetHandler";
    /**
     * outbound http编码器
     */
    public static final String HTTP_ENCODER = "httpEncoder";

    @Override
    protected void initChannel(Channel channel) throws Exception {
        channel.pipeline().addLast(HTTP_REQUEST_DECODER, new HttpRequestDecoder()) //in bound http 解码器  netty自带的http协议解码器
                .addLast(HTTP_AGGREGATOR, new HttpObjectAggregator(65536))
                .addLast(HTTP_ENCODER, new HttpResponseEncoder());// outbound http编码器 netty 自带的http协议编码器
    }
}
