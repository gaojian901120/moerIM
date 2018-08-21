package com.moer.handler;

import com.alibaba.fastjson.JSON;
import com.moer.bean.GroupInfo;
import com.moer.bean.GroupMembers;
import com.moer.entity.ImGroup;
import com.moer.entity.ImMessage;
import com.moer.entity.ImSession;
import com.moer.entity.ImUser;
import com.moer.l2.L2ApplicationContext;
import com.moer.service.GroupInfoService;
import com.moer.service.ServiceFactory;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Vector;

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
        Map<Integer,GroupMembers> memberMap = targetGroup.userList;
        Map<Integer,GroupMembers> blackMap = targetGroup.blackList;
        for (GroupMembers members : memberMap.values()) {
            int uid = members.getUid();
            if (blackMap.containsKey(uid)){
                continue;
            }
            Map<String,ImSession> userSessions = imUserContext.get(uid).getSessions();
            //说明用户不在线
            if (userSessions == null  || userSessions.size() == 0){
                continue;
            }
            for (ImSession session : userSessions.values()){
                Channel channel = session.getChannel();
                session.setUpdateTime(System.currentTimeMillis());
                if (channel != null) {
                    Vector<ImMessage> imMessages = session.getMsgQueue();
                    if (imMessages == null) {
                        imMessages = new Vector<>();
                        session.setMsgQueue(imMessages);
                    }
                    if (channel.isActive()) {
                        imMessages.add(imMessage);
                        L2ApplicationContext.getInstance().sendResponse(channel, JSON.toJSONString(imMessages));
                        imMessages.clear();
                    }else {
                        //session 对应的请求 还没有过来 保持在服务器上临时存储
                        imMessages.add(imMessage);
                    }
                }
            }

        }
        System.out.println(System.currentTimeMillis());
    }
}
