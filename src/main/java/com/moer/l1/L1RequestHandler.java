package com.moer.l1;

import com.alibaba.fastjson.JSON;
import com.moer.l1.bean.InitResponseBean;
import com.moer.l1.business.ActionHandler;
import com.moer.zookeeper.NodeManager;
import com.moer.zookeeper.ServerNode;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelMetadata;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaoxuejian on 2018/5/18.
 */
public class L1RequestHandler extends ChannelInboundHandlerAdapter {
    public static Logger log = LoggerFactory.getLogger(L1RequestHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            String uri = request.uri();
            String[] uriArr = uri.split("\\?");
            ActionHandler actionHandler = new ActionHandler();
            String response = "";
            if (uriArr[0].equals("/init")) {
                response = actionHandler.init(ctx, request);
            } else {
                response = "invalid request: " + uri;
            }
            byte[] responseB = new byte[0];
            try {
                responseB = response.getBytes("UTF-8");
            } catch (Exception e) {
                log.error("encode response {} error with exception : {}", responseB, e.getMessage());
            }
            FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.OK, Unpooled.wrappedBuffer(responseB));
            httpResponse.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
            httpResponse.headers().set(HttpHeaders.Names.CONTENT_LENGTH, httpResponse.content().readableBytes());
            httpResponse.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            ctx.write(httpResponse);
            ctx.flush();
        }
    }
}
