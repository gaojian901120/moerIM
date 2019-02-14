package com.moer.l2.business;

import com.alibaba.fastjson.JSON;
import com.moer.common.Constant;
import com.moer.common.TraceLogger;
import com.moer.config.ImConfig;
import com.moer.entity.ImSession;
import com.moer.l2.L2ApplicationContext;
import com.moer.l2.TimerTask;
import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by gaoxuejian on 2018/5/1.
 * 业务线程池
 */
public class AsyncActionHandler {
    private static final ExecutorService executor = new ThreadPoolExecutor(20, 100, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(100000));//CPU核数4-10倍

    public static void doLogin(ChannelHandlerContext ctx, ImSession imSession,Map<String,String> paramMap) {
        //异步线程池处理
        executor.submit(() -> {
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
                                L2ApplicationContext.getInstance().sessionLogout(session.getValue(), "user login in other app end",Constant.CODE_MULTI_END_ERROR);
                                TraceLogger.trace(Constant.USER_SESSION_TRACE,"user {} session {} logout because new session {} login, current config:[{}], request source:{} ",
                                        session.getValue().getUid(), session.getValue().getSeeesionId(), imSession.getSeeesionId(), JSON.toJSONString(imConfig), source);
                            }
                        }
                    }
                }
                if (!imConfig.isMultiWebEnd() && source.equals(ImSession.SESSION_SOURCE_WEB)) { //保证web端 只有一个用户可以登录
                    if (onlineSession != null && onlineSession.size()>0) { //剔除所有该用户已经登陆的web会话
                        for (Map.Entry<String, ImSession> session : onlineSession.entrySet()) {
                            if (session.getValue().getSource().equals(ImSession.SESSION_SOURCE_WEB)) {
                                L2ApplicationContext.getInstance().sessionLogout(session.getValue(), "user login in other web end",Constant.CODE_MULTI_END_ERROR);
                                TraceLogger.trace(Constant.USER_SESSION_TRACE,"user {} session {} logout because new session {} login, current config:[{}], request source:{} ",
                                        session.getValue().getUid(), session.getValue().getSeeesionId(), imSession.getSeeesionId(), JSON.toJSONString(imConfig), source);
                            }
                        }
                    }
                }
                if (!imConfig.isMultiEnd()) { // 保证pc和app 只有一个用户可以登录
                    if (onlineSession != null && onlineSession.size()>0) { //剔除所有该用户已经登陆的web会话
                        for (Map.Entry<String, ImSession> session : onlineSession.entrySet()) {
                            if (source.equals(ImSession.SESSION_SOURCE_APP) && session.getValue().getSource().equals(ImSession.SESSION_SOURCE_WEB)) {
                                L2ApplicationContext.getInstance().sessionLogout(session.getValue(), "user login in other end",Constant.CODE_MULTI_END_ERROR);
                            } else if (source.equals(ImSession.SESSION_SOURCE_WEB) && session.getValue().getSource().equals(ImSession.SESSION_SOURCE_APP)) {
                                L2ApplicationContext.getInstance().sessionLogout(session.getValue(), "user login in other end",Constant.CODE_MULTI_END_ERROR);
                            }
                            TraceLogger.trace(Constant.USER_SESSION_TRACE,"user {}, session {} logout because new session {} login, current config:[{}], request source:{} ",
                                    session.getValue().getUid(), session.getValue().getSeeesionId(), imSession.getSeeesionId(), JSON.toJSONString(imConfig), source);
                        }
                    }
                }

                L2ApplicationContext.getInstance().sessionLogin(imSession);
                L2ApplicationContext.getInstance().timerThread.taskLisk.add(new TimerTask(imSession.getUpdateTime() + 10000, TimerTask.TASK_SESSION_CHECK, imSession));
                Map<String, Object> map = new HashMap<>();
                map.put("code", Constant.CODE_SUCCESS);
                map.put("message", "Connect success");
                Map<String,Object> data = new HashMap<>();
                data.put("sessionId", imSession.getSeeesionId());
                data.put("uid",String.valueOf(uid));
                data.put("token",token);
                map.put("data", data);
                String response = JSON.toJSONString(map);
                if("web".equals(imSession.getSource())){
                    response = "connectCallback (" + response + ")";
                }
                L2ApplicationContext.getInstance().sendHttpResp(ctx.channel(),response,true);
                TraceLogger.trace(Constant.USER_SESSION_TRACE,"user {} session {} login success",imSession.getUid(), imSession.getSeeesionId());
                }
            catch (Exception e) {
            }
        });
    }
}
