package com.moer.common;

import com.moer.config.NettyConfig;
import com.moer.util.ConfigUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslContext;

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
    private SslContext sslContext = null;
    private NettyConfig nettyConfig;
    public HttpChannelInitializer() throws Exception{
        super();
//        KeyStore ks = KeyStore.getInstance("JKS");
//        InputStream ksInputStream = this.getClass().getClassLoader().getResourceAsStream("moer.jks");
//        ks.load(ksInputStream,"moerim".toCharArray());
//        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
//        kmf.init(ks,"moerim".toCharArray());
//        List<String> ciphers = new ArrayList<>();
//        ciphers.add("ECDHE-RSA-AES128-SHA");
//        ciphers.add("ECDHE-RSA-AES256-SHA");
//        ciphers.add("AES128-SHA");
//        ciphers.add("AES256-SHA");
//        ciphers.add("DES-CBC3-SHA");
//        sslContext = SslContextBuilder.forServer(kmf).sslProvider(SslProvider.OPENSSL).build();
        nettyConfig = ConfigUtil.loadNettyConfig();
    }


    @Override
    protected void initChannel(Channel channel) throws Exception {
//        SSLEngine sslEngine = sslContext.newEngine(channel.alloc());
//        sslEngine.setNeedClientAuth(false);
//        SslHandler sslHandler = new SslHandler(sslEngine);
        channel.pipeline()
//                .addFirst(sslHandler)
                .addLast(HTTP_REQUEST_DECODER, new HttpRequestDecoder()) //in bound http 解码器  netty自带的http协议解码器
                .addLast(HTTP_AGGREGATOR, new HttpObjectAggregator(65536))
                .addLast(HTTP_ENCODER, new HttpResponseEncoder());
        if(nettyConfig.isUseHttpCompress()){
            channel.pipeline().addLast("HttpContentCompressor",new HttpContentCompressor());
        };
    }
}
