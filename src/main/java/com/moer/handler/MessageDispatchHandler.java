package com.moer.handler;

import com.alibaba.fastjson.JSON;
import com.moer.bean.GroupInfo;
import com.moer.bean.GroupMembers;
import com.moer.entity.ImGroup;
import com.moer.entity.ImMessage;
import com.moer.entity.ImSession;
import com.moer.entity.ImUser;
import com.moer.l2.L2ApplicationContext;
import com.moer.server.PushMessageServer;
import com.moer.service.GroupInfoService;
import com.moer.service.ServiceFactory;
import com.moer.store.GroupStore;
import com.moer.store.UserStore;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by gaoxuejian on 2018/5/3.
 */
public class MessageDispatchHandler implements Runnable, Comparable<MessageDispatchHandler> {
    public static Logger logger = LoggerFactory.getLogger(MessageDispatchHandler.class);
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
        int sender = Integer.valueOf(imMessage.getSend());
        int recver = Integer.valueOf(imMessage.getRecv());
        Map<Integer, ImUser> imUserContext = L2ApplicationContext.getInstance().IMUserContext;
        Map<Integer, ImGroup> imGroupContext = L2ApplicationContext.getInstance().IMGroupContext;
        ImGroup targetGroup = imGroupContext.get(recver);
        if (targetGroup == null){
            GroupInfoService infoService = ServiceFactory.getInstace(GroupInfoService.class);
            GroupInfo groupInfo= infoService.getById(recver);
            targetGroup = ImGroup.initImGroup(groupInfo);
            imGroupContext.put(recver,targetGroup);
            logger.info("init group {} ",recver);
        }
        Map<Integer,GroupMembers> onlineMap = targetGroup.userList;
        Map<Integer,GroupMembers> blackMap = targetGroup.blackList;
        for (GroupMembers members : onlineMap.values()) {
            int uid = members.getUid();
            if (blackMap.containsKey(uid)){
                continue;
            }
            Map<String,ImSession> userSessions = imUserContext.get(uid).getSessions();
            //说明用户不在线 @TODO 直接清理用户在线数据 并更新未读消息数据
            if (userSessions == null  || userSessions.size() == 0){
                continue;
            }
            for (ImSession session : userSessions.values()){
                Channel channel = session.getChannel();
                if (channel != null && channel.isOpen()) {
                    try {
                        List<ImMessage> imMessages = session.getMsgQueue();
                        if (imMessages == null){
                            imMessages = new ArrayList<>();
                            session.setMsgQueue(imMessages);
                        }
                        imMessages.add(imMessage);
                        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.OK, Unpooled.wrappedBuffer(JSON.toJSONString(imMessages).getBytes("UTF-8")));
                        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
                        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
                        response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                        channel.write(response);
                        channel.flush();
                        imMessages.clear();
                    } catch (Exception e) {

                    }
                }
            }

        }
        System.out.println(System.currentTimeMillis());
    }
}
