package com.moer.handler;

import com.moer.L2ApplicationContext;
import com.moer.common.ActionHandler;
import com.moer.common.Constant;
import com.moer.common.TraceLogger;
import com.moer.entity.ImSession;
import com.moer.entity.ImUser;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaoxuejian on 2019/3/18.
 */
public class ServiceRequestHandler extends ChannelInboundHandlerAdapter {
    public static Logger logger = LoggerFactory.getLogger(ServiceRequestHandler.class);
    protected String sessionId;

    protected ActionHandler actionHandler;
    protected String method;
    protected String uid;
    protected String from;
    protected String uri;
    public ServiceRequestHandler(ActionHandler actionHandler) {
        this.actionHandler = actionHandler;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent)evt;
            if(event.state() == IdleState.READER_IDLE){
                ctx.channel().close();
            }
        }else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String response = "";
        try {
            if (msg instanceof HttpRequest) {
                HttpRequest request = (HttpRequest) msg;
                uri = request.uri();

                String[] uriArr = uri.split("\\?");
                String[] uriArr2 = uriArr[0].split("/");
                method = uriArr2[uriArr2.length - 1];
                parseRequest(request);
                try {
                    Method method1 = actionHandler.getClass().getMethod(method, ChannelHandlerContext.class, HttpRequest.class);
                    response = method1.invoke(actionHandler, ctx, request).toString();
                }catch (NoSuchMethodException me){
                    response = ActionHandler.renderResult(Constant.CODE_PARAM_ERROR,"method "+ method +"not exist",null);;
                    logger.warn("method {} not exist uri {} in channel {}", method, uri, ctx.channel().id().asLongText());
                }
                catch (Exception e) {
                    response = ActionHandler.renderResult(Constant.CODE_PARAM_ERROR,"request exception: " + e.getCause().getMessage(),null);;
                    logger.warn("request handler error with uri {} in channel {}", uri, ctx.channel().id().asLongText());
                }

            }
        }catch (Exception e){
            response = ActionHandler.renderResult(Constant.CODE_PARAM_ERROR,"request parse error: " + e.getCause().getMessage(),null);;
            logger.error(e.getMessage(),e);
        }
        finally {
//            TraceLogger.trace(Constant.MONITOR_TRACE, "113600815 uri:{}, resp:{}" ,uri,response);
            if (!response.equals(Constant.ASYNC)) {
                if("web".equals(from)){
                    response = method + "Callback (" + response + ")";
                }

                ActionHandler.sendHttpResp(ctx.channel(),response, false);
            }
            ReferenceCountUtil.release(msg);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        TraceLogger.trace(Constant.MONITOR_TRACE,uid + "uid: " + uid + " method: " + method + " exception: "+ cause.getMessage());

        ctx.close();
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }



    private void parseRequest(HttpRequest request){
        String uri = request.uri();
        QueryStringDecoder decoder = new QueryStringDecoder(uri);
        Map<String, String> paramMap = new HashMap<>();
        decoder.parameters().entrySet().forEach(entry -> {
            paramMap.put(entry.getKey(), entry.getValue().get(0));
            if(entry.getKey().equals("from")){
                from = entry.getValue().get(0);
            }
            if("pull".equals(method)){
                if(entry.getKey().equals("sessionid")){
                    sessionId = entry.getValue().get(0);
                }
            }
        });
        from = paramMap.get("from");
        sessionId = paramMap.get("sessionid");
        uid = ActionHandler.getLoginUid(request.headers(),paramMap);

    }
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        if("pull".equals(method)){//pull请求发生异常 将状态设置为unpulling 并更新时间 防止客户端重连间隔将其清空
            ImUser user = L2ApplicationContext.getInstance().getImUserFromContext(Integer.valueOf(uid));
            if(user != null){
                Map<String,ImSession> sessionMap = user.getSessions();
                if(sessionMap != null){
                    ImSession session = sessionMap.get(sessionId);
                    if(session != null){
                        session.setStatus(ImSession.SESSION_STATUS_UNPULL);
                        session.setUpdateTime(System.currentTimeMillis());
                    }

                }
            }
        }

        super.channelUnregistered(ctx);
    }

}
