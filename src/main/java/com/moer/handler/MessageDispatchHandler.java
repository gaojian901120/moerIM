package com.moer.handler;

import com.alibaba.fastjson.JSON;
import com.moer.entity.ImGroup;
import com.moer.entity.ImMessage;
import com.moer.entity.ImUser;
import com.moer.l2.L2ApplicationContext;
import com.moer.server.PushMessageServer;
import com.moer.store.GroupStore;
import com.moer.store.UserStore;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by gaoxuejian on 2018/5/3.
 */
public class MessageDispatchHandler implements Runnable, Comparable<MessageDispatchHandler> {
    private int priority;
    private ImMessage imMessage;

    public MessageDispatchHandler(int priority, ImMessage imMessage) {
        this.priority = priority;
        this.imMessage = imMessage;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public int compareTo(MessageDispatchHandler o) {
        if (this.priority < o.priority) {
            return 1;
        }
        if (this.priority > o.priority) {
            return -1;
        }
        return 0;
    }

    @Override
    public void run() {
        //分发消息到不同的连接层节点
        int sender = imMessage.getSend();
        int recver = imMessage.getRecv();
        Map<Integer, ImUser> imUserContext = L2ApplicationContext.getInstance().IMUserContext;
        Map<Integer, ImGroup> imGroupContext = L2ApplicationContext.getInstance().IMGroupContext;
        Set<Integer> onlineGroupUser = groupStore.getAllGroupOnlineUser(recver);
        for (Integer uid : onlineGroupUser) {
            List<ImMessage> messageList = new ArrayList<ImMessage>();
            messageList.add(imMessage);
            userStore.pushMessage(uid, messageList);
            Channel channel = application.getUserStore().getChannel(uid);
            if (channel != null && channel.isOpen()) {
                try {
                    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.OK, Unpooled.wrappedBuffer(JSON.toJSONString(userStore.popAllMessage(uid)).getBytes("UTF-8")));
                    response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
                    response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
                    response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                    channel.write(response);
                    channel.flush();
                } catch (Exception e) {

                }
            }
        }
        System.out.println(System.currentTimeMillis());
    }
}
