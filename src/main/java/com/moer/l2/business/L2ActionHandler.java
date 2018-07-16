package com.moer.l2.business;

import com.alibaba.fastjson.JSON;
import com.moer.common.ActionHandler;
import com.moer.config.ImConfig;
import com.moer.config.NettyConfig;
import com.moer.entity.ImMessage;
import com.moer.entity.ImSession;
import com.moer.l1.bean.InitResponseBean;
import com.moer.l2.L2ApplicationContext;
import com.moer.l2.TimerTask;
import com.moer.server.BusinessServer;
import com.moer.util.CryptUtil;
import com.moer.zookeeper.NodeManager;
import com.moer.zookeeper.ServerNode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gaoxuejian on 2018/5/19.
 * 线程安全 所以单例就行了
 */
public class L2ActionHandler extends ActionHandler {
    public static Logger logger = LoggerFactory.getLogger(L2ActionHandler.class);

    /**
     * GET /connect?uid=xxx&token=xxx&source=xx
     * uid 表示用户uid
     * token 包含服务器的校验信息
     * source 表示请求来源 web or app
     * @param context
     * @param request
     * @return
     */
    public String connect(ChannelHandlerContext context, HttpRequest request) {
        HttpMethod method = request.method();
        Map<String, String> paramMap = new HashMap<>();
        String result = "";
        if (!method.equals(HttpMethod.GET)) {
            return renderResult(1001, "invalid request method", null);
        }
        String uri = request.uri();
        QueryStringDecoder decoder = new QueryStringDecoder(uri);
        decoder.parameters().entrySet().forEach(entry -> {
            paramMap.put(entry.getKey(), entry.getValue().get(0));
        });
        //@TODO uid不再通过参数传递  通过解密参数获取
        if (!paramMap.containsKey("uid") || !paramMap.containsKey("token") || !paramMap.containsKey("source")) {
            return renderResult(1001, "invalid request params", null);
        }
        int uid = Integer.valueOf(paramMap.get("uid"));
        String token = paramMap.get("token");
        String source = paramMap.get("source");
        if (!source.equals(ImSession.SESSION_SOURCE_APP) && !source.equals(ImSession.SESSION_SOURCE_WEB)) {
            return renderResult(1001, "invalid request source", null);
        }

        NodeManager nodeManager = NodeManager.getInstance();
        String serverHash = nodeManager.getNodeHash();
        if (!token.equals(serverHash)) {
            return renderResult(1001, "invalid server node，please refresh server info", null);
        }
        //每次连接生成一个新的session
        ImSession imSession = new ImSession();
        imSession.setChannel(context.channel());
        imSession.setUid(uid);
        imSession.setCreateTime(System.currentTimeMillis());
        imSession.setUpdateTime(System.currentTimeMillis());
        imSession.setSource(source);
        imSession.setStatus(ImSession.SESSION_STATUS_NORMAL);
        imSession.setSeeesionId(CryptUtil.str2HexStr(uid + ImSession.sessionCode + System.currentTimeMillis()));
        //判断多端登录
        Map<String,ImSession> onlineSession = L2ApplicationContext.getInstance().getUserOnlineSession(uid);
        ImConfig imConfig = L2ApplicationContext.getInstance().imConfig;
        NettyConfig nettyConfig = L2ApplicationContext.getInstance().nettyConfig;
        if (!imConfig.isMultiAppEnd() && source.equals(ImSession.SESSION_SOURCE_APP)) { //保证app只有一个用户可以登录
            if (onlineSession != null) { //剔除所有该用户已经登陆的app会话
                for (Map.Entry<String, ImSession> session : onlineSession.entrySet()) {
                    if (session.getValue().getSource().equals(ImSession.SESSION_SOURCE_APP)) {
                        L2ApplicationContext.getInstance().logout(imSession, "user login in other app end");
                    }
                }
            }
        }
        if (!imConfig.isMultiWebEnd() && source.equals(ImSession.SESSION_SOURCE_WEB)) { //保证web端 只有一个用户可以登录
            if (onlineSession != null) { //剔除所有该用户已经登陆的web会话
                for (Map.Entry<String, ImSession> session : onlineSession.entrySet()) {
                    if (session.getValue().getSource().equals(ImSession.SESSION_SOURCE_WEB)) {
                        L2ApplicationContext.getInstance().logout(imSession, "user login in other web end");
                    }
                }
            }
        }
        if (!imConfig.isMultiEnd()) { // 保证pc和app 只有一个用户可以登录
            if (onlineSession != null) { //剔除所有该用户已经登陆的web会话
                for (Map.Entry<String, ImSession> session : onlineSession.entrySet()) {
                    if (source.equals(ImSession.SESSION_SOURCE_APP) && session.getValue().getSource().equals(ImSession.SESSION_SOURCE_WEB)) {
                        L2ApplicationContext.getInstance().logout(imSession, "user login in other end");
                    } else if (source.equals(ImSession.SESSION_SOURCE_WEB) && session.getValue().getSource().equals(ImSession.SESSION_SOURCE_APP)) {
                        L2ApplicationContext.getInstance().logout(imSession, "user login in other end");
                    }
                }
            }
        }
        ServerNode serverNode = nodeManager.getServerNode(uid);
        if (serverNode != null && serverNode.getHost().equals(nettyConfig.getHostName()) && serverNode.getPort() == nettyConfig.getPort()) {
            if (onlineSession == null) {
                onlineSession = new HashMap<>();
            }
            L2ApplicationContext.getInstance().login(imSession);
            L2ApplicationContext.getInstance().timerThread.taskLisk.add(new TimerTask(imSession.getUpdateTime() + 10000, TimerTask.TASK_SESSION_CHECK, imSession));
            result = renderResult(1000, "connect success", imSession.getSeeesionId());
        } else {
            result = renderResult(1002, "the user request the error service node", null);
        }
        return result;
    }


    /**
     * 获取最新消息
     * GET /pull?sessionid=xxx&uid=xxx
     *
     * @param context
     * @param request
     * @return
     */
    public String pull(ChannelHandlerContext context, HttpRequest request) {
        HttpMethod method = request.method();
        Map<String, String> paramMap = new HashMap<>();
        String result = "";
        if (!method.equals(HttpMethod.GET)) {
            return renderResult(1001, "invalid request method", null);
        }
        String uri = request.uri();
        QueryStringDecoder decoder = new QueryStringDecoder(uri);
        decoder.parameters().entrySet().forEach(entry -> {
            paramMap.put(entry.getKey(), entry.getValue().get(0));
        });
        //@TODO uid不再通过参数传递  通过解密参数获取
        if (!paramMap.containsKey("sessionid") || !paramMap.containsKey("uid")) {
            return renderResult(1001, "invalid request params", null);
        }
        int uid = Integer.valueOf(paramMap.get("uid"));
        String sessionId = paramMap.get("sessionid");
        Map<String, ImSession> sessionMap = L2ApplicationContext.getInstance().getUserOnlineSession(uid);
        if (sessionMap == null || !sessionMap.containsKey(sessionId)) {
            return renderResult(1001, "please connect the server node first", null);
        }

        ImSession imSession = sessionMap.get(sessionId);
        if (imSession.getChannel().isActive()){
            return renderResult(1001, "failed", "other user connect with this session");
        }
        // 更新session活跃时间
        imSession.setUpdateTime(System.currentTimeMillis());
        List<ImMessage> messageList = imSession.getMsgQueue();
        if (messageList != null && messageList.size() > 0) {
            return renderResult(1000, "success", JSON.toJSON(messageList));
        } else {
            //没有数据 需要hold 业务线程池进行处理后续任务
            //模拟异步提交任务
            //BusinessServer.doBusiness(context,request);
            return "asynchandle";
        }
    }

}
