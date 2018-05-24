package com.moer.l2.business;

import com.alibaba.fastjson.JSON;
import com.moer.config.ImConfig;
import com.moer.entity.ImSession;
import com.moer.l1.bean.InitResponseBean;
import com.moer.l2.L2ApplicationContext;
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
import java.util.Map;

/**
 * Created by gaoxuejian on 2018/5/19.
 */
public class ActionHandler {
    public static Logger log = LoggerFactory.getLogger(ActionHandler.class);

    /**
     * GET /connect?uid=xxx&token=xxx
     * uid 表示用户uid
     * token 包含服务器的校验信息
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
            // entry.getValue()是一个List, 只取第一个元素
            paramMap.put(entry.getKey(), entry.getValue().get(0));
        });
        if (!paramMap.containsKey("uid") || !paramMap.containsKey("token")) {
            return renderResult(1001, "invalid request params", null);
        }
        int uid = Integer.valueOf(paramMap.get("uid"));
        String token = paramMap.get("token");
        NodeManager nodeManager = NodeManager.getInstance();
        String serverHash = nodeManager.getNodeHash();
        if (token != serverHash) {
            return renderResult(1001, "invalid server node，please refresh serveri nfo", null);
        }
        //判断多端登录
        Map<String,ImSession> onlineSession = L2ApplicationContext.getInstance().userContext.getUserOnlineSession(uid);
        ImConfig imConfig = L2ApplicationContext.getInstance().imConfig;
        if (imConfig.isMultiAppEnd() )
//        InitResponseBean irb = new InitResponseBean();
//        if (method.equals(HttpMethod.GET)) {
//
//            // GET /init?uid=100809070
//            HttpHeaders headers = request.headers();
//            String cookie = headers.get("Cookie");
//            if (cookie != null && cookie.contains("_jm_ppt_id")) {
//
//                String uid = parmMap.get("uid");
//                ServerNode serverNode = nodeManager.getServerNode(Integer.valueOf(uid));
//                if (serverNode != null) {
//                    irb.addr = serverNode.getHost() + ":" + serverNode.getPort();
//                    irb.token = "hello";
//                    result = renderResult(1000, "success", irb);
//                } else {
//                    result = renderResult(1002, "no server to service", null);
//                }
//            } else {
//                result = renderResult(1001, "user not login", null);
//            }
//
//        } else {
//            result = renderResult(1001, "user not login", null);
//        }
        return result;
    }

    public String renderResult(int code, String message, Object data) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("message", message);
        map.put("data", data);
        return JSON.toJSONString(map);
    }
}
