package com.moer.handler;

import com.moer.common.ActionHandler;
import com.moer.l2.L2ApplicationContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
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
            String[] uriArr2 = uriArr[0].split("/");
            String method = uriArr2[uriArr2.length - 1];
            try {
                Method method1 = actionHandler.getClass().getMethod(method, ChannelHandlerContext.class, HttpRequest.class);
                response = method1.invoke(actionHandler, ctx, request).toString();
            } catch (Exception e) {
                response = "request exception: " + e.getMessage();
            }
            if (!response.equals("asynchandle")) {
                if(method.equals("pull")){
                    L2ApplicationContext.getInstance().sendHttpResp(ctx.channel(),response, false);
                }else {
                    L2ApplicationContext.getInstance().sendHttpResp(ctx.channel(),response, true);
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("channel exceptionCaught" + ctx.channel().id().asShortText());
        //ctx.close();
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel channelInactive:" + ctx.channel().id().asShortText());

        super.channelInactive(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel channelUnregistered" + ctx.channel().id().asShortText());
        super.channelUnregistered(ctx);
    }
}
