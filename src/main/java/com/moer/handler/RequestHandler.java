package com.moer.handler;

import com.moer.common.ActionHandler;
import com.moer.entity.ImSession;
import com.moer.entity.ImUser;
import com.moer.l2.L2ApplicationContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by gaoxuejian on 2018/6/5.
 */
public class RequestHandler extends ChannelInboundHandlerAdapter {
    public static Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private ActionHandler actionHandler;
    private String method;
    private String sessionId;
    private String uid;
    public RequestHandler(ActionHandler actionHandler) {
        this.actionHandler = actionHandler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof HttpRequest) {
                HttpRequest request = (HttpRequest) msg;
                String uri = request.uri();
                String[] uriArr = uri.split("\\?");
                String response = "";
                String[] uriArr2 = uriArr[0].split("/");
                method = uriArr2[uriArr2.length - 1];
                parseRequest(request);
                try {
                    Method method1 = actionHandler.getClass().getMethod(method, ChannelHandlerContext.class, HttpRequest.class);
                    response = method1.invoke(actionHandler, ctx, request).toString();
                }catch (NoSuchMethodException me){
                    response = "method "+ method +"not exist";
                    logger.warn("method {} not exist uri {} in channel {}", method, uri, ctx.channel().id().asLongText());
                }
                catch (Exception e) {
                    response = "request exception: " + e.getMessage();
                    logger.warn("request handler error with uri {} in channel {}", uri, ctx.channel().id().asLongText());
                }
                if (!response.equals("asynchandle")) {
                    if(method.equals("pull")){
                        L2ApplicationContext.getInstance().sendHttpResp(ctx.channel(),response, false);
                    }else {
                        L2ApplicationContext.getInstance().sendHttpResp(ctx.channel(),response, true);
                    }
                }
            }
        }catch (Exception e){
            logger.warn("request parse  error in channel {}", ctx.channel().id().asLongText());
            logger.error(e.getMessage(),e);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        if("pull".equals(method)){//pull请求发生异常 将状态设置为unpulling 并更新时间 防止客户端重连间隔将其清空
            ImUser user = L2ApplicationContext.getInstance().IMUserContext.get(Integer.valueOf(uid));
            if(user != null){
                Map<String,ImSession> sessionMap = user.getSessions();
                if(sessionMap != null){
                    ImSession session = sessionMap.get(sessionId);
                    session.setStatus(ImSession.SESSION_STATUS_UNPULL);
                    session.setUpdateTime(System.currentTimeMillis());
                }
            }
        }

        super.channelUnregistered(ctx);
    }

    private void parseRequest(HttpRequest request){
        String uri = request.uri();
        QueryStringDecoder decoder = new QueryStringDecoder(uri);
        decoder.parameters().entrySet().forEach(entry -> {
            if("pull".equals(method)){
                if(entry.getKey().equals("sessionid")){
                    sessionId = entry.getValue().get(0);
                }
                if(entry.getKey().equals("uid")){
                    uid = entry.getValue().get(0);
                }
            }
        });
    }
}
