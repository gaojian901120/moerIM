package com.moer.l2.business;

import com.alibaba.fastjson.JSON;
import com.moer.common.ActionHandler;
import com.moer.common.Constant;
import com.moer.common.ServiceFactory;
import com.moer.config.ImConfig;
import com.moer.config.NettyConfig;
import com.moer.entity.ImMessage;
import com.moer.entity.ImSession;
import com.moer.entity.ImUser;
import com.moer.l2.L2ApplicationContext;
import com.moer.l2.TimerTask;
import com.moer.redis.RedisStore;
import com.moer.util.CryptUtil;
import com.moer.zookeeper.NodeManager;
import com.moer.zookeeper.ServerNode;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
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
            return renderResult(Constant.CODE_INVALID_REQUEST_METHOD, null);
        }
        String uri = request.uri();
        QueryStringDecoder decoder = new QueryStringDecoder(uri);
        decoder.parameters().entrySet().forEach(entry -> {
            paramMap.put(entry.getKey(), entry.getValue().get(0));
        });
        //@TODO uid不再通过参数传递  通过解密参数获取
        if (!paramMap.containsKey("uid") || !paramMap.containsKey("token") || !paramMap.containsKey("source")) {
            return renderResult(Constant.CODE_PARAM_ERROR, null);
        }
        int uid = Integer.valueOf(paramMap.get("uid"));
        String token = paramMap.get("token");
        String source = paramMap.get("source");
        if (!source.equals(ImSession.SESSION_SOURCE_APP) && !source.equals(ImSession.SESSION_SOURCE_WEB)) {
            return renderResult(Constant.CODE_INVALID_SOURCE, null);
        }

        NodeManager nodeManager = NodeManager.getInstance();
        ServerNode serverNode = nodeManager.getServerNode(uid);
        NettyConfig nettyConfig = L2ApplicationContext.getInstance().nettyConfig;

        if (!(serverNode != null && serverNode.getHost().equals(nettyConfig.getHostName()) && serverNode.getPort() == nettyConfig.getPort())) {
            return renderResult(Constant.CODE_NODE_EXPIRED, null);
        }

        //每次连接生成一个新的session
        ImSession imSession = new ImSession();
        imSession.setChannel(context.channel());
        imSession.setUid(uid);
        imSession.setCreateTime(System.currentTimeMillis());
        imSession.setUpdateTime(System.currentTimeMillis());
        imSession.setSource(source);
        imSession.setStatus(ImSession.SESSION_STATUS_UNPULL);
        imSession.setSeeesionId(CryptUtil.str2HexStr(uid + ImSession.sessionCode + System.currentTimeMillis()));
        //判断多端登录
        Map<String,ImSession> onlineSession = L2ApplicationContext.getInstance().getUserOnlineSession(uid);
        ImConfig imConfig = L2ApplicationContext.getInstance().imConfig;
        if (!imConfig.isMultiAppEnd() && source.equals(ImSession.SESSION_SOURCE_APP)) { //保证app只有一个用户可以登录
            if (onlineSession != null && onlineSession.size()>0) { //剔除所有该用户已经登陆的app会话
                for (Map.Entry<String, ImSession> session : onlineSession.entrySet()) {
                    if (session.getValue().getSource().equals(ImSession.SESSION_SOURCE_APP)) {
                        L2ApplicationContext.getInstance().sessionLogout(session.getValue(), "user login in other app end",Constant.CODE_MULTI_END_ERROR);
                    }
                }
            }
        }
        if (!imConfig.isMultiWebEnd() && source.equals(ImSession.SESSION_SOURCE_WEB)) { //保证web端 只有一个用户可以登录
            if (onlineSession != null && onlineSession.size()>0) { //剔除所有该用户已经登陆的web会话
                for (Map.Entry<String, ImSession> session : onlineSession.entrySet()) {
                    if (session.getValue().getSource().equals(ImSession.SESSION_SOURCE_WEB)) {
                        L2ApplicationContext.getInstance().sessionLogout(session.getValue(), "user login in other web end",Constant.CODE_MULTI_END_ERROR);
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
                }
            }
        }

            L2ApplicationContext.getInstance().sessionLogin(imSession);
            L2ApplicationContext.getInstance().timerThread.taskLisk.add(new TimerTask(imSession.getUpdateTime() + 10000, TimerTask.TASK_SESSION_CHECK, imSession));
            Map<String,Object> data = new HashMap<>();
            System.out.println("connect:" + " uid: " + uid + " channelid: " + imSession.getChannel().id().asShortText());
            data.put("sessionId", imSession.getSeeesionId());
            data.put("uid",String.valueOf(uid));
            data.put("token",token);
            result = renderResult(1000, "connect success", data);
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
            return renderResult(Constant.CODE_INVALID_REQUEST_METHOD, null);
        }
        String uri = request.uri();
        QueryStringDecoder decoder = new QueryStringDecoder(uri);
        decoder.parameters().entrySet().forEach(entry -> {
            paramMap.put(entry.getKey(), entry.getValue().get(0));
        });
        //@TODO uid不再通过参数传递  通过解密参数获取
        if (!paramMap.containsKey("sessionid") || !paramMap.containsKey("uid")) {
            return renderResult(Constant.CODE_PARAM_ERROR, null);
        }
        int uid = Integer.valueOf(paramMap.get("uid"));
        String sessionId = paramMap.get("sessionid");
        String token = paramMap.get("token");
        NodeManager nodeManager = NodeManager.getInstance();
        ServerNode serverNode = nodeManager.getServerNode(uid);
        NettyConfig nettyConfig = L2ApplicationContext.getInstance().nettyConfig;
        if (!(serverNode != null && serverNode.getHost().equals(nettyConfig.getHostName()) && serverNode.getPort() == nettyConfig.getPort())) {
            return renderResult(Constant.CODE_NODE_EXPIRED, null);
        }
        Map<String, ImSession> sessionMap = L2ApplicationContext.getInstance().getUserOnlineSession(uid);
        if (sessionMap == null || !sessionMap.containsKey(sessionId)) {
            return renderResult(Constant.CODE_UNCONNECT, null);
        }

        ImSession imSession = sessionMap.get(sessionId);

        // 更新session活跃时间
        imSession.setUpdateTime(System.currentTimeMillis());
        RedisStore redisStore = ServiceFactory.getRedis();
        redisStore.hset(Constant.REDIS_USER_ONLINE_SET, uid+"",System.currentTimeMillis()+"");

        imSession.setChannel(context.channel());//连接不相等说明channel改变了
        System.out.println("pull:" + " uid: " + uid + " channelid: " + imSession.getChannel().id().asShortText());
        List<ImMessage> messageList = imSession.popAllMsgQueue();

        if (messageList != null && messageList.size() > 0) {
            imSession.setStatus(ImSession.SESSION_STATUS_UNPULL);
            System.out.println("message size: " +  messageList.size());
            Collections.sort(messageList);
            return renderResult(Constant.CODE_SUCCESS, JSON.toJSON(L2ApplicationContext.getInstance().convertMessage(messageList)));
        } else {
            imSession.setStatus(ImSession.SESSION_STATUS_PULLING);
            sessionMap.put(sessionId,imSession);
            System.out.println("webSessionId:" + imSession.getSeeesionId() +" Uid: " + imSession.getUid() + "ChannelId: " + context.channel().id());

            //没有数据 需要hold 业务线程池进行处理后续任务
            return "asynchandle";
        }
    }

    //节点状态检查接口
    public String status(ChannelHandlerContext context, HttpRequest request){
        HttpMethod method = request.method();
        Map<String, String> paramMap = new HashMap<>();
        String result = "";
        if (!method.equals(HttpMethod.GET)) {
            return renderResult(Constant.CODE_INVALID_REQUEST_METHOD, null);
        }
        String uri = request.uri();
        QueryStringDecoder decoder = new QueryStringDecoder(uri);
        decoder.parameters().entrySet().forEach(entry -> {
            paramMap.put(entry.getKey(), entry.getValue().get(0));
        });
        if(!paramMap.containsKey("action")){
            return renderResult(Constant.CODE_PARAM_ERROR, null);
        }
        String action = paramMap.get("action");
        //查看所有连接的channel的列表
        if(action.equals("channels")){
            Map<Integer,ImUser> userMap =  L2ApplicationContext.getInstance().IMUserContext;
            Map<String,Channel> resultMap = new HashMap<>();
            for (Map.Entry<Integer, ImUser> userEntry: userMap.entrySet()) {
                Map<String,ImSession> sessionMap = userEntry.getValue().getSessions();
                for (Map.Entry<String, ImSession> sessionEntry :  sessionMap.entrySet()) {
                    resultMap.put(sessionEntry.getValue().getChannel().id().asLongText(),sessionEntry.getValue().getChannel());
                }
            }
            return renderResult(Constant.CODE_SUCCESS, resultMap);
        }else if(action.equals("context")){
            Map<String , Object> resultMap = new HashMap<>();
            resultMap.put("groups",L2ApplicationContext.getInstance().IMGroupContext);
            resultMap.put("users",L2ApplicationContext.getInstance().IMUserContext);
            return renderResult(Constant.CODE_SUCCESS,resultMap);
        }
        return renderResult(Constant.CODE_SUCCESS, null);
    }
}
