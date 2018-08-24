package com.moer.common;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Created by gaoxuejian on 2018/6/5.
 */
public class RequestHandler extends ChannelInboundHandlerAdapter {
    public static Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private ActionHandler actionHandler;

    public RequestHandler(ActionHandler actionHandler) {
        this.actionHandler = actionHandler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            String uri = request.uri();
            String[] uriArr = uri.split("\\?");
            String response = "";
            try {
                String[] uriArr2 = uriArr[0].split("/");
                String method = uriArr2[uriArr2.length - 1];
                Method method1 = actionHandler.getClass().getMethod(method, ChannelHandlerContext.class, HttpRequest.class);
                response = method1.invoke(actionHandler, ctx, request).toString();
            } catch (Exception e) {
                response = "request exception: " + e.getMessage();
            }
            if (!response.equals("asynchandle")) {
                byte[] responseB = new byte[0];
                try {
                    responseB = response.getBytes("UTF-8");
                } catch (Exception e) {
                    logger.error("encode response {} error with exception : {}", responseB, e.getMessage());
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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("channel exceptionCaught");
        //ctx.close();
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel channelInactive");
        super.channelInactive(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel channelUnregistered");
        //super.channelUnregistered(ctx);
    }
}
