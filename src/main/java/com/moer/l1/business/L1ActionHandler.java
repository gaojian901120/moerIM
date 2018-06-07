package com.moer.l1.business;

import com.alibaba.fastjson.JSON;
import com.moer.common.ActionHandler;
import com.moer.l1.bean.InitResponseBean;
import com.moer.util.CryptUtil;
import com.moer.zookeeper.NodeManager;
import com.moer.zookeeper.ServerNode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaoxuejian on 2018/5/19.
 */
public class L1ActionHandler extends ActionHandler {
    public static Logger logger = LoggerFactory.getLogger(L1ActionHandler.class);


    //初始化方法 获取服务端的信息
    // GET /init?uid=100809070
    public String init(ChannelHandlerContext context, HttpRequest request) {
        HttpMethod method = request.method();
        Map<String, String> paramMap = new HashMap<>();
        NodeManager nodeManager = NodeManager.getInstance();
        InitResponseBean irb = new InitResponseBean();
        String result = "";
        if (method.equals(HttpMethod.GET)) {
            String uri = request.uri();
            HttpHeaders headers = request.headers();
            String cookie = headers.get("Cookie");
            if (cookie != null && cookie.contains("_jm_ppt_id")) {
                //@TODO 根据pptid 获取登陆用户信息 到时候就不需要传递uid了
                //String decode = CryptUtil.authcode("WPLNkDBlepHjloJo9_IHSjxvqGvPkRcgA6PEhyO-3CsTebUCjBORaEotA6LtYzQYUk6iUAMq1ho4InH1bJjG2V4mpIT5yLeUM_J1UDACQbXmyv-beDaSQqNG","293nAs9u23l&29",CryptUtil.DiscuzAuthcodeMode.Decode,0);
                QueryStringDecoder decoder = new QueryStringDecoder(uri);
                decoder.parameters().entrySet().forEach(entry -> {
                    // entry.getValue()是一个List, 只取第一个元素
                    paramMap.put(entry.getKey(), entry.getValue().get(0));
                });
                String uid = paramMap.get("uid");
                ServerNode serverNode = nodeManager.getServerNode(Integer.valueOf(uid));
                if (serverNode != null) {
                    irb.addr = serverNode.getHost() + ":" + serverNode.getPort();
                    irb.token = nodeManager.getNodeHash();
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
