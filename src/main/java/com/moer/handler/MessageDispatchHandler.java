package com.moer.handler;

import com.alibaba.fastjson.JSON;
import com.moer.bean.GroupInfo;
import com.moer.bean.GroupMembers;
import com.moer.common.Constant;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Created by gaoxuejian on 2018/5/3.
 */
public class MessageDispatchHandler implements Runnable, Comparable<MessageDispatchHandler> {
    public static Logger logger = LoggerFactory.getLogger(MessageDispatchHandler.class);
    private int priority; //消息优先级 默认为5   值越大 优先级越高
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
        System.out.println("当前投递的消息的优先级为：" + priority);
        int chatType = imMessage.getChatType();
        Map<Integer, ImUser> imUserContext = L2ApplicationContext.getInstance().IMUserContext;
        int sender = Integer.valueOf(imMessage.getSend());
        String recver = imMessage.getRecv();
        if (chatType == 2) { //群聊
            Map<String, ImGroup> imGroupContext = L2ApplicationContext.getInstance().IMGroupContext;
            ImGroup targetGroup = imGroupContext.get(recver);
            if (targetGroup == null) {
                GroupInfoService infoService = ServiceFactory.getInstace(GroupInfoService.class);
                GroupInfo groupInfo = infoService.getByGid(String.valueOf(recver));
                targetGroup = ImGroup.initImGroup(groupInfo);
                imGroupContext.put(String.valueOf(recver), targetGroup);
                logger.info("init group {} ", recver);
            }
            Map<Integer, GroupMembers> memberMap = targetGroup.userList;
            Map<Integer, Integer> blackMap = L2ApplicationContext.getInstance().IMUserContext.get(targetGroup.groupInfo.getOwner()).userBlackList;
            for (GroupMembers members : memberMap.values()) {
                int uid = members.getUid();
                if (blackMap.containsKey(uid)) {
                    continue;
                }
                Map<String, ImSession> userSessions = imUserContext.get(uid).getSessions();
                dispatchMsgInSessions(userSessions);
            }
        }else if (chatType == 1) {//单聊
            Map<String, ImSession> userSessions = imUserContext.get(Integer.valueOf(recver)).getSessions();

            //说明用户不在线
            dispatchMsgInSessions(userSessions);

        }
    }
    private void dispatchMsgInSessions(Map<String, ImSession> userSessions)
    {
        System.out.println("userSessions:" +userSessions);
        if (userSessions == null || userSessions.size() == 0) {
            return;
        }
        for (ImSession session : userSessions.values()) {
            Channel channel = session.getChannel();
            session.setUpdateTime(System.currentTimeMillis());
            System.out.println("sessionActive:" + channel.isActive());
            System.out.println("sessionStatus:" + session.getStatus());
            System.out.println("SessionId:" + session.getSeeesionId() +" Uid: " + session.getUid() + "ChannelId: " + session.getChannel().id());

            if (channel != null) {
                //channel活跃只表示socket有效 可能多个请求使用同一个channel
                if (session.getStatus() == 0) {
                    Vector<ImMessage> imMessages = session.popAllMsgQueue();
                    imMessages.add(imMessage);
                    Collections.sort(imMessages);
                    Map<String, Object> data = new HashMap<>();
                    data.put("code", Constant.CODE_SUCCESS);
                    data.put("message", "push message success");
                    data.put("data", imMessages);
                    System.out.println("message size: " + imMessages.size());
                    L2ApplicationContext.getInstance().sendResponse(channel, JSON.toJSONString(data));
                    session.setStatus(-1);

                } else {
                    //session 对应的请求 还没有过来 保持在服务器上临时存储
                    session.pushMsg(imMessage);
                }
            }
        }
    }
}
