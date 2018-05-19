package com.moer.l1.business;

import com.alibaba.fastjson.JSON;
import com.moer.l1.bean.InitResponseBean;
import com.moer.zookeeper.NodeManager;
import com.moer.zookeeper.ServerNode;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaoxuejian on 2018/5/19.
 */
public class ActionHandler {
    public static Logger log = LoggerFactory.getLogger(ActionHandler.class);

    public String init(ChannelHandlerContext context, HttpRequest request) {
        HttpMethod method = request.method();
        Map<String, String> parmMap = new HashMap<>();
        NodeManager nodeManager = NodeManager.getInstance();
        InitResponseBean irb = new InitResponseBean();
        String result = "";
        if (method.equals(HttpMethod.GET)) {
            String uri = request.uri();
            // GET /init?uid=100809070
            HttpHeaders headers = request.headers();
            String cookie = headers.get("Cookie");
            if (cookie != null && cookie.contains("_jm_ppt_id")) {
                QueryStringDecoder decoder = new QueryStringDecoder(uri);
                decoder.parameters().entrySet().forEach(entry -> {
                    // entry.getValue()是一个List, 只取第一个元素
                    parmMap.put(entry.getKey(), entry.getValue().get(0));
                });
                String uid = parmMap.get("uid");
                ServerNode serverNode = nodeManager.getServerNode(Integer.valueOf(uid));
                if (serverNode != null) {
                    irb.addr = serverNode.getHost() + ":" + serverNode.getPort();
                    irb.token = "hello";
                    result = renderResult(1000, "success", irb);
                } else {
                    result = renderResult(1002, "no server to service", null);
                }
            } else {
                result = renderResult(1001, "user not login", null);
            }

        } else {
            result = renderResult(1001, "user not login", null);
        }
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
