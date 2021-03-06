package com.moer.business;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.moer.L2ApplicationContext;
import com.moer.common.ActionHandler;
import com.moer.common.Constant;
import com.moer.common.ServiceFactory;
import com.moer.common.TraceLogger;
import com.moer.config.NettyConfig;
import com.moer.entity.ImGroup;
import com.moer.entity.ImMessage;
import com.moer.entity.ImSession;
import com.moer.entity.ImUser;
import com.moer.redis.RedisStore;
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
    public String connect(ChannelHandlerContext context, HttpRequest request) throws Exception {
        HttpMethod method = request.method();
        Map<String, String> paramMap = new HashMap<>();
        if (!method.equals(HttpMethod.GET)) {
            return renderResult(Constant.CODE_INVALID_REQUEST_METHOD, null);
        }
        String uri = request.uri();
        QueryStringDecoder decoder = new QueryStringDecoder(uri);
        decoder.parameters().entrySet().forEach(entry -> {
            paramMap.put(entry.getKey(), entry.getValue().get(0));
        });
        String from = paramMap.get("from");
        if (!paramMap.containsKey("source")) {
            return renderResult(Constant.CODE_PARAM_ERROR, null);
        }
        String source = paramMap.get("source");
        if (!source.equals(ImSession.SESSION_SOURCE_APP) && !source.equals(ImSession.SESSION_SOURCE_WEB)) {
            return renderResult(Constant.CODE_INVALID_SOURCE, null);
        }
        HttpHeaders headers = request.headers();
        String suid = getLoginUid(headers,paramMap);
        if(suid.length() == 0){
            return renderResult(Constant.CODE_PARAM_ERROR, null);
        }
        int uid = Integer.valueOf(suid);



        NodeManager nodeManager = NodeManager.getInstance();
        ServerNode serverNode = nodeManager.getServerNode(uid);
        NettyConfig nettyConfig = L2ApplicationContext.getInstance().nettyConfig;

        if (!(serverNode != null && serverNode.getHost().equals(nettyConfig.getHostName()) && serverNode.getPort() == nettyConfig.getPort())) {
            TraceLogger.trace(Constant.USER_SESSION_TRACE,"connect node expire,data->serverNode:{},nettConfig:{},uid:{},params:{}",JSON.toJSONString(serverNode),JSON.toJSONString(nettyConfig),uid,JSON.toJSONString(paramMap));
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
        //同步处理登陆问题
        return AsyncActionHandler.doLoginSync(context,imSession,paramMap);


    }


    /**
     * 获取最新消息
     * GET /pull?sessionid=xxx&uid=xxx
     *
     * @param context
     * @param request
     * @return
     */
    public String pull(ChannelHandlerContext context, HttpRequest request) throws Exception {
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
        String from = paramMap.get("from");
        HttpHeaders headers = request.headers();
        String suid = getLoginUid(headers,paramMap);
        if(suid.length() == 0){
            return renderResult(Constant.CODE_UNLOGIN, null);
        }
        if (!paramMap.containsKey("sessionid")) {
            return renderResult(Constant.CODE_PARAM_ERROR, null);
        }
        int uid = Integer.valueOf(suid);

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
        //session状态 过期 则重新请求
        if(imSession.getStatus() == ImSession.SESSION_STATUS_EXPIRED){
            return renderResult(Constant.CODE_UNLOGIN, null);
        }else if(imSession.getStatus() == ImSession.SESSION_STATUS_PULLING){ //说明可能出现问题 导致连续出现了两个pull 但是sessionId是一个
            if (!imSession.getChannel().id().asLongText().equals(context.channel().id().asLongText())){
                if (imSession.getChannel().isOpen()) {
                    imSession.getChannel().close();
                }
            }else {
                return renderResult(Constant.CODE_PARAM_ERROR,"multi pull request", null);
            }
        }
        // 更新session活跃时间
        imSession.setUpdateTime(System.currentTimeMillis());
        RedisStore redisStore = ServiceFactory.getRedis();
        redisStore.hset(Constant.REDIS_USER_ONLINE_SET, uid+"",System.currentTimeMillis()+"");
        if(!imSession.getChannel().id().asLongText().equals(context.channel().id().asLongText())){
            imSession.setChannel(context.channel());//连接不相等说明channel改变了
        }
        List<ImMessage> messageList = imSession.popMsgFromTail();
        if (messageList != null && messageList.size() > 0) {
            synchronized (imSession.getMsgLock()) {
                imSession.setStatus(ImSession.SESSION_STATUS_UNPULL);
                Collections.sort(messageList);
                StringBuffer midSb = new StringBuffer();
                messageList.forEach(item -> {
                    midSb.append(item.getMid());
                    midSb.append(",");
                });
                TraceLogger.trace(Constant.MESSAGE_TRACE, "push message {} to user {} with sessionId {} and channelId {} in pull sync request", midSb.toString(), uid, imSession.getSeeesionId(), context.channel().id().asShortText());
                Map<String, Object> data = new HashMap<>();
                data.put("code", Constant.CODE_SUCCESS);
                data.put("message", "push message success");
                data.put("data", L2ApplicationContext.getInstance().convertMessage(messageList));
                String response = JSON.toJSONString(data);
                data.clear();
                messageList.clear();
                return response;
            }
        } else {
            imSession.setStatus(ImSession.SESSION_STATUS_PULLING);
            sessionMap.put(sessionId,imSession);
            //没有数据 需要hold 业务线程池进行处理后续任务
            return Constant.ASYNC;
        }
    }

    //节点状态检查接口
    public String status(ChannelHandlerContext context, HttpRequest request) {
        HttpMethod method = request.method();
        Map<String, String> paramMap = new HashMap<>();
        if (!method.equals(HttpMethod.GET)) {
            return renderResult(Constant.CODE_INVALID_REQUEST_METHOD, null);
        }
        String uri = request.uri();
        QueryStringDecoder decoder = new QueryStringDecoder(uri);
        decoder.parameters().entrySet().forEach(entry -> {
            paramMap.put(entry.getKey(), entry.getValue().get(0));
        });
        JSONObject jsonResult = new JSONObject();
        Map<Integer, ImUser> imUserMap = L2ApplicationContext.getInstance().getIMUserContext();
        JSONArray userlist = new JSONArray();
        String action = paramMap.get("action");
        if("total".equals(action)){
            JSONObject result  = new JSONObject();
            result.put("totalUser",L2ApplicationContext.getInstance().getIMUserContext().size());
            return renderResult(Constant.CODE_SUCCESS, result);
        }
        imUserMap.forEach((gid, user) -> {
            JSONObject item = new JSONObject();
            item.put("uid", user.getUid());
            item.put("groupMap", user.getGroupMap().values());
            Map<String, ImSession> sessionMap = user.getSessions();
            JSONArray sessionList = new JSONArray();
            sessionMap.forEach((sid, session) -> {
                JSONObject sessionItem = new JSONObject();
                sessionItem.put("channelId", session.getChannel().id().asLongText());
                sessionItem.put("updateTime", session.getUpdateTime());
                sessionItem.put("createTime", session.getCreateTime());
                sessionItem.put("sessionId", session.getSeeesionId());
                sessionItem.put("uid", session.getUid());
                sessionItem.put("status", session.getStatus());
                sessionItem.put("source", session.getSource());
                sessionList.add(sessionItem);
            });
            item.put("sessionList", sessionList);
            userlist.add(item);
        });
        JSONArray groupList = new JSONArray();
        Map<String,ImGroup> imGroupMap =  L2ApplicationContext.getInstance().getIMGroupContext();
        imGroupMap.forEach((gid,group)->{
            JSONObject item = new JSONObject();
            item.put("gid",group.gid);
            item.put("info",group.groupInfo);
            item.put("memberList",group.getUserList().values());
            groupList.add(item);
        });
        jsonResult.put("groups", groupList);
        jsonResult.put("users", userlist);
        return renderResult(Constant.CODE_SUCCESS, jsonResult);
    }
}
