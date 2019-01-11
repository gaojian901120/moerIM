package com.moer.l2;

import com.moer.handler.RequestHandler;
import com.moer.config.NettyConfig;
import com.moer.l2.business.L2ActionHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by gaoxuejian on 2018/5/1.
 * 初始化启动时候的Channel Handler
 */
public class L2ChannelInitializer extends ChannelInitializer<Channel> {
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
     * inbound 解析出消息之后进行认证
     */
    public static final String AUTHORIZE_HANDLER = "authorizeHandler";
    /**
     * inbound 长轮训 链接处理
     */
    public static final String XHR_POLLING_TRANSPORT = "xhrPollingTransport";
    /**
     * outbound http编码器
     */
    public static final String HTTP_ENCODER = "httpEncoder";
    /**
     * outbound http 响应内容压缩
     */
    public static final String HTTP_COMPRESSION = "httpCompression";
    /**
     * outbound  ssl 支持
     */
    public static final String SSL_HANDLER = "ssl";
    /**
     * outbound
     */
    public static final String SOCKETIO_ENCODER = "socketioEncoder";

    private static final Logger log = LoggerFactory.getLogger(L2ChannelInitializer.class);

    private NettyConfig nettyConfig;
    private PushMessageServer application;

    public L2ChannelInitializer(NettyConfig nettyConfig, PushMessageServer application) {
        this.nettyConfig = nettyConfig;
        this.application = application;

//        PacketEncoder encoder = new PacketEncoder(configuration, jsonSupport);
//        PacketDecoder decoder = new PacketDecoder(jsonSupport, ackManager);
//
//        String connectPath = configuration.getContext() + "/";
//
//        boolean isSsl = configuration.getKeyStore() != null;
//        if (isSsl) {
//            try {
//                sslContext = createSSLContext(configuration);
//            } catch (Exception e) {
//                throw new IllegalStateException(e);
//            }

//        StoreFactory factory = configuration.getStoreFactory();
//        authorizeHandler = new AuthorizeHandler(connectPath, scheduler, configuration, namespacesHub, factory, this, ackManager, clientsBox);
//        factory.init(namespacesHub, authorizeHandler, jsonSupport);
//        xhrPollingTransport = new PollingTransport(decoder, authorizeHandler, clientsBox);
//        webSocketTransport = new WebSocketTransport(isSsl, authorizeHandler, configuration, scheduler, clientsBox);
//
//        PacketListener packetListener = new PacketListener(ackManager, namespacesHub, xhrPollingTransport, scheduler);
//
//
//        packetHandler = new InPacketHandler(packetListener, decoder, namespacesHub, configuration.getExceptionListener());
//
//        try {
//            encoderHandler = new EncoderHandler(configuration, encoder);
//        } catch (Exception e) {
//            throw new IllegalStateException(e);
//        }
//
//        wrongUrlHandler = new WrongUrlHandler();
    }

    protected void initChannel(Channel channel) throws Exception {
        channel.pipeline().addLast(HTTP_REQUEST_DECODER, new HttpRequestDecoder()) //in bound http 解码器  netty自带的http协议解码器
                .addLast(HTTP_AGGREGATOR, new HttpObjectAggregator(65536))
                .addLast(HTTP_ENCODER, new HttpResponseEncoder())// outbound http编码器 netty 自带的http协议编码器
                .addLast(PACKET_HANDLER, new RequestHandler(new L2ActionHandler()));// 注意顺序 这个必须是在第一个


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
