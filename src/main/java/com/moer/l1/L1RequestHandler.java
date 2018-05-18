package com.moer.l1;

import com.alibaba.fastjson.JSON;
import com.moer.entity.ImMessage;
import com.moer.zookeeper.NodeManager;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelMetadata;
import io.netty.handler.codec.http.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gaoxuejian on 2018/5/18.
 */
public class L1RequestHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            NodeManager nodeManager = NodeManager.getInstance();
            HttpRequest request = (HttpRequest) msg;
            String uri = request.uri();
            Map<String, String> parmMap = new HashMap<>();
            HttpMethod method = request.method();
            // 只接受POST请求  GET 请求用于测试@TODO 上线前需要修改
            if (method.equals(HttpMethod.GET)) {
                String[] uriArr = uri.split("\\?");
                QueryStringDecoder decoder = new QueryStringDecoder(uri);
                decoder.parameters().entrySet().forEach(entry -> {
                    // entry.getValue()是一个List, 只取第一个元素
                    parmMap.put(entry.getKey(), entry.getValue().get(0));
                });
                String uid = parmMap.get("uid");
            }


            ChannelMetadata metadata = ctx.channel().metadata();
//            List<ImMessage> pushedMessage = application.getUserStore().popAllMessage(Integer.parseInt(uid));
//            if (pushedMessage == null || pushedMessage.size() == 0) {
//                //BusinessServer.doBusiness(ctx, msg);
//                application.getUserStore().addChannel(Integer.parseInt(uid), ctx.channel());
//
//            } else {
//                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.OK, Unpooled.wrappedBuffer(JSON.toJSONString(pushedMessage).getBytes("UTF-8")));
//                response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
//                response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
//                response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
//                ctx.write(response);
//                ctx.flush();
//            }
        }
    }
}
