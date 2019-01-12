package com.moer.handler;

import com.alibaba.fastjson.JSON;
import com.moer.bean.GroupInfo;
import com.moer.bean.GroupMembers;
import com.moer.common.Constant;
import com.moer.common.ServiceFactory;
import com.moer.entity.ImGroup;
import com.moer.entity.ImMessage;
import com.moer.entity.ImSession;
import com.moer.entity.ImUser;
import com.moer.l2.L2ApplicationContext;
import com.moer.service.GroupInfoService;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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
        logger.trace(Constant.MESSAGE_TRACE,"message dispatch handler get message: {}", imMessage.getMid());
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
        try {
            //分发消息到不同的连接层节点
            int chatType = imMessage.getChatType();
            Map<Integer, ImUser> imUserContext = L2ApplicationContext.getInstance().IMUserContext;
            int sender = Integer.valueOf(imMessage.getSend());
            String recver = imMessage.getRecv();
            if (chatType == 2) { //群聊
                logger.trace(Constant.MESSAGE_TRACE,"begin dispatch group message {} to group {}", imMessage.getMid(), recver);
                Map<String, ImGroup> imGroupContext = L2ApplicationContext.getInstance().IMGroupContext;
                ImGroup targetGroup = imGroupContext.get(recver);
                if (targetGroup == null) {
                    GroupInfoService infoService = ServiceFactory.getInstace(GroupInfoService.class);
                    GroupInfo groupInfo = infoService.getByGid(String.valueOf(recver));
                    if(groupInfo == null){
                        logger.trace(Constant.MESSAGE_TRACE, "message {} dispatch failed because receive group {} not exist", imMessage.getMid(), recver);
                        return;
                    }
                    targetGroup = L2ApplicationContext.getInstance().initImGroup(groupInfo);
                    imGroupContext.put(String.valueOf(recver), targetGroup);
                    logger.trace(Constant.MESSAGE_TRACE, "while dispatch message {}, put group {} into ImGroupContext",imMessage.getMid(), recver);
                }
                Map<Integer, GroupMembers> memberMap = targetGroup.getUserList();
                Set<Integer> userBlackList =  L2ApplicationContext.getInstance().UserBlackContext.get(targetGroup.groupInfo.getOwner());
                StringBuffer blackUserSb = new StringBuffer();
                StringBuffer pushedUserSb = new StringBuffer();
                if(memberMap != null){
                    for (GroupMembers members : memberMap.values()) {
                        int uid = members.getUid();
                        if (userBlackList != null && userBlackList.contains(uid)) {
                            blackUserSb.append(uid);
                            blackUserSb.append(",");
                            continue;
                        }
                        //过滤登录用户
                        if(sender == uid){
                            continue;
                        }
                        dispatchMsgInSessions(uid);
                        pushedUserSb.append(uid);
                        pushedUserSb.append(",");
                    }
                    logger.trace(Constant.MESSAGE_TRACE,"message {} dispatch detail: total onlineUser[{}], pushedDetailUser[{}], blackUser[{}]", imMessage.getMid(), memberMap.size(), pushedUserSb.toString(), blackUserSb.toString());
                }
            }else if (chatType == 1) {//单聊
                logger.trace(Constant.MESSAGE_TRACE,"begin dispatch private message {} to user {}", imMessage.getMid(), recver);
                dispatchMsgInSessions(Integer.valueOf(recver));
            }
        }catch (Exception e){
            logger.warn("message {} dispatch handler error: {}", imMessage.getMid(), e.getMessage());
            logger.error(e.getMessage(),e );
        }

    }
    private void dispatchMsgInSessions(int uid)
    {
        ImUser imUser = L2ApplicationContext.getInstance().IMUserContext.get(uid);
        if(imUser == null){
            logger.trace(Constant.MESSAGE_TRACE,"while push message {}, user {} not login, no message to push", imMessage.getMid(), uid);
            return;
        }
        Map<String, ImSession> userSessions = imUser.getSessions();
        if (userSessions == null || userSessions.size() == 0) {
            logger.trace(Constant.MESSAGE_TRACE,"while push message {}, user {} session is empty, no message to push", imMessage.getMid(), uid);
            return;
        }
        for (ImSession session : userSessions.values()) {
            Channel channel = session.getChannel();
            session.setUpdateTime(System.currentTimeMillis());
            if (channel.isActive() && session.isVaild()) {
                //channel活跃只表示socket有效 可能多个请求使用同一个channel
                if (session.getStatus() == ImSession.SESSION_STATUS_PULLING) {
                    Vector<ImMessage> imMessages = session.popAllMsgQueue();
                    StringBuffer midSb = new StringBuffer();
                    imMessages.add(imMessage);
                    imMessages.forEach(item->{midSb.append(item.getMid());midSb.append(",");});
                    Collections.sort(imMessages);
                    Map<String, Object> data = new HashMap<>();
                    data.put("code", Constant.CODE_SUCCESS);
                    data.put("message", "push message success");
                    data.put("data", L2ApplicationContext.getInstance().convertMessage(imMessages));
                    System.out.println("message size: " + imMessages.size());
                    L2ApplicationContext.getInstance().sendHttpResp(channel, JSON.toJSONString(data),false);
                    session.setStatus(ImSession.SESSION_STATUS_UNPULL);
                    logger.trace(Constant.MESSAGE_TRACE, "push message {} to user {} with sessionId {} and channelId {}", midSb.toString(), uid, session.getSeeesionId(), channel.id().asShortText());
                } else {
                    //session 对应的请求 还没有过来 保持在服务器上临时存储
                    session.pushMsg(imMessage);
                    logger.trace(Constant.MESSAGE_TRACE, "pull request not coming while push message {} to user {} with sessionId {} and channelId {}, cache message {} on server", imMessage.getMid(), uid, session.getSeeesionId(), channel.id().asShortText(), imMessage.getMid());
                }
            }
        }
    }
}
