package com.moer.business;

import com.alibaba.fastjson.JSON;
import com.moer.L2ApplicationContext;
import com.moer.common.ActionHandler;
import com.moer.common.Constant;
import com.moer.common.TraceLogger;
import com.moer.config.ImConfig;
import com.moer.entity.ImSession;
import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static com.moer.common.ActionHandler.renderResult;
import static com.moer.common.Constant.ASYNC;

/**
 * Created by gaoxuejian on 2018/5/1.
 * 业务线程池
 */
public class AsyncActionHandler {
//    private static final ExecutorService executor = new ThreadPoolExecutor(16, 16, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(100000));//CPU核数4-10倍
private static final ExecutorService executor = null;
    public static String doLoginAsync(ChannelHandlerContext ctx, ImSession imSession,Map<String,String> paramMap) {
        //异步线程池处理
        executor.submit(() -> {
            String response = doLoginSync(ctx,imSession,paramMap);
            if("web".equals(imSession.getSource())){
                response = "connectCallback (" + response + ")";
            }
            ActionHandler.sendHttpResp(ctx.channel(),response,false);
            TraceLogger.trace(Constant.USER_SESSION_TRACE,"user {} session {} login success",imSession.getUid(), imSession.getSeeesionId());
        });
        return ASYNC;
    }
    public static String  doLoginSync(ChannelHandlerContext ctx, ImSession imSession,Map<String,String> paramMap){
        String response;

        try {
            int uid = imSession.getUid();
            String token = paramMap.get("token");
            String source = paramMap.get("source");
            Map<String,ImSession> onlineSession = L2ApplicationContext.getInstance().getUserOnlineSession(uid);
            ImConfig imConfig = L2ApplicationContext.getInstance().imConfig;
            TraceLogger.trace(Constant.USER_SESSION_TRACE, "user {} new session generate with sessionId {},",uid, imSession.getSeeesionId());
            if (!imConfig.isMultiAppEnd() && source.equals(ImSession.SESSION_SOURCE_APP)) { //保证app只有一个用户可以登录
                if (onlineSession != null && onlineSession.size()>0) { //剔除所有该用户已经登陆的app会话
                    for (Map.Entry<String, ImSession> session : onlineSession.entrySet()) {
                        if (session.getValue().getSource().equals(ImSession.SESSION_SOURCE_APP)) {
                            if (!session.getValue().getSeeesionId().equals(imSession.getSeeesionId())) {
                                L2ApplicationContext.getInstance().sessionLogout(session.getValue(), "user login in other app end", Constant.CODE_MULTI_END_ERROR);
                                TraceLogger.trace(Constant.USER_SESSION_TRACE, "user {} session {} logout because new session {} login, current config:[{}], request source:{} ",
                                        session.getValue().getUid(), session.getValue().getSeeesionId(), imSession.getSeeesionId(), JSON.toJSONString(imConfig), source);
                            }
                        }
                    }
                }
            }
            if (!imConfig.isMultiWebEnd() && source.equals(ImSession.SESSION_SOURCE_WEB)) { //保证web端 只有一个用户可以登录
                if (onlineSession != null && onlineSession.size()>0) { //剔除所有该用户已经登陆的web会话
                    for (Map.Entry<String, ImSession> session : onlineSession.entrySet()) {
                        if (session.getValue().getSource().equals(ImSession.SESSION_SOURCE_WEB)) {
                            if(!session.getValue().getSeeesionId().equals(imSession.getSeeesionId())){
                                L2ApplicationContext.getInstance().sessionLogout(session.getValue(), "user login in other web end",Constant.CODE_MULTI_END_ERROR);
                                TraceLogger.trace(Constant.USER_SESSION_TRACE,"user {} session {} logout because new session {} login, current config:[{}], request source:{} ",
                                        session.getValue().getUid(), session.getValue().getSeeesionId(), imSession.getSeeesionId(), JSON.toJSONString(imConfig), source);
                            }
                        }
                    }
                }
            }
            if (!imConfig.isMultiEnd()) { // 保证pc和app 只有一个用户可以登录
                if (onlineSession != null && onlineSession.size()>0) { //剔除所有该用户已经登陆的web会话
                    for (Map.Entry<String, ImSession> session : onlineSession.entrySet()) {
                        if (!session.getValue().getSeeesionId().equals(imSession.getSeeesionId())) {
                            if (source.equals(ImSession.SESSION_SOURCE_APP) && session.getValue().getSource().equals(ImSession.SESSION_SOURCE_WEB)) {
                                L2ApplicationContext.getInstance().sessionLogout(session.getValue(), "user login in other end", Constant.CODE_MULTI_END_ERROR);
                            } else if (source.equals(ImSession.SESSION_SOURCE_WEB) && session.getValue().getSource().equals(ImSession.SESSION_SOURCE_APP)) {
                                L2ApplicationContext.getInstance().sessionLogout(session.getValue(), "user login in other end", Constant.CODE_MULTI_END_ERROR);
                            }
                            TraceLogger.trace(Constant.USER_SESSION_TRACE, "user {}, session {} logout because new session {} login, current config:[{}], request source:{} ",
                                    session.getValue().getUid(), session.getValue().getSeeesionId(), imSession.getSeeesionId(), JSON.toJSONString(imConfig), source);
                        }
                    }
                }
            }

            L2ApplicationContext.getInstance().sessionLogin(imSession);
            Map<String, Object> map = new HashMap<>();
            map.put("code", Constant.CODE_SUCCESS);
            map.put("message", "Connect success");
            Map<String,Object> data = new HashMap<>();
            data.put("sessionId", imSession.getSeeesionId());
            data.put("uid",String.valueOf(uid));
            data.put("token",token);
            map.put("data", data);
            response = JSON.toJSONString(map);
            return response;
        }
        catch (Exception e) {
            return renderResult(Constant.CODE_PARAM_ERROR, e.getCause().getMessage());
        }
    }
}
