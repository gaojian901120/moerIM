package com.moer.l1.business;

import com.moer.common.ActionHandler;
import com.moer.common.Constant;
import com.moer.l1.bean.InitResponseBean;
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
public class L1ActionHandler extends ActionHandler {
    public static Logger logger = LoggerFactory.getLogger(L1ActionHandler.class);


    //初始化方法 获取服务端的信息
    // GET /init?uid=100809070&source=xxx
    public String init(ChannelHandlerContext context, HttpRequest request) {
        HttpMethod method = request.method();
        Map<String, String> paramMap = new HashMap<>();
        NodeManager nodeManager = NodeManager.getInstance();

        InitResponseBean irb = new InitResponseBean();
        String result = "";
        if (method.equals(HttpMethod.GET)) {
            String uri = request.uri();
            QueryStringDecoder decoder = new QueryStringDecoder(uri);
            decoder.parameters().entrySet().forEach(entry -> {
                paramMap.put(entry.getKey(), entry.getValue().get(0));
            });
            String from = paramMap.get("from");
            HttpHeaders headers = request.headers();
            String uid = getLoginUid(headers,from);
            if(uid.length() > 0){
                ServerNode serverNode = nodeManager.getServerNode(Integer.valueOf(uid));
                if (serverNode != null) {
                    irb.addr = serverNode.getHost() + ":" + serverNode.getPort();
                    irb.token = nodeManager.getNodeHash();
                    irb.uid =uid;
                    result = renderResult(Constant.CODE_SUCCESS, irb);
                } else {
                    result = renderResult(Constant.CODE_NO_SERVER_NODE, null);
                }
            } else {
                result = renderResult(Constant.CODE_UNLOGIN, null);
            }
        } else {
            result = renderResult(Constant.CODE_INVALID_REQUEST_METHOD, null);
        }
        return result;
    }
}
