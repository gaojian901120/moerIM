package com.moer.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.moer.bean.GroupMembers;
import com.moer.common.Constant;
import com.moer.common.TraceLogger;
import com.moer.entity.ImGroup;
import com.moer.entity.ImMessage;
import com.moer.entity.ImSession;
import com.moer.entity.ImUser;
import com.moer.L2ApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

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
        TraceLogger.trace(Constant.MESSAGE_TRACE,"message dispatch handler get message: {}", imMessage.getMid());
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
            Map<Integer, ImUser> imUserContext = L2ApplicationContext.getInstance().getIMUserContext();
            if(imMessage.getSend().equals("admin") ) {
                imMessage.setSend("0");
            }
            String recver = imMessage.getRecv();
            if (chatType == 2) { //群聊
                TraceLogger.trace(Constant.MESSAGE_TRACE,"begin dispatch group message {} to group {}", imMessage.getMid(), recver);
                ImGroup imGroup = L2ApplicationContext.getInstance().getImGroupInContext(recver);
                if(imGroup == null){
                    return;
                }
                Map<Integer, GroupMembers> memberMap = imGroup.getUserList();
                Set<Integer> userBlackList =  L2ApplicationContext.getInstance().UserBlackContext.get(imGroup.groupInfo.getOwner());
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
                        dispatchMsgInSessions(uid);
                        pushedUserSb.append(uid);
                        pushedUserSb.append(",");
                    }
                }

                TraceLogger.trace(Constant.MESSAGE_TRACE,"message {} dispatch detail: total onlineUser[{}], pushedDetailUser[{}], blackUser[{}]", imMessage.getMid(), memberMap.size(), pushedUserSb.toString(), blackUserSb.toString());
            }else if (chatType == 1) {//单聊
                if(recver.equals("0")){
                    String extp = imMessage.getExtp();
                    JSONObject extpObj = JSON.parseObject(extp);
                    String users = extpObj.getString("scope");
                    if("all".equals(users)){
                        imUserContext.forEach((kuid,vUserContext)->{
                            TraceLogger.trace(Constant.MESSAGE_TRACE,"begin dispatch private message {} to user {}", imMessage.getMid(), kuid);
                            dispatchMsgInSessions(kuid);
                        });
                    }else {
                        String [] userArr = users.split(",");
                        for (String user:userArr) {
                            TraceLogger.trace(Constant.MESSAGE_TRACE,"begin dispatch private message {} to user {}", imMessage.getMid(), user);
                            dispatchMsgInSessions(Integer.valueOf(user));
                        }
                    }

                }
                else {
                    TraceLogger.trace(Constant.MESSAGE_TRACE,"begin dispatch private message {} to user {}", imMessage.getMid(), recver);
                    dispatchMsgInSessions(Integer.valueOf(recver));
                }
            }
        }catch (Exception e){
            logger.warn("message {} dispatch handler error: {}", imMessage.getMid(), e.getMessage());
            logger.error(e.getMessage(),e );
        }

    }
    private void dispatchMsgInSessions(int uid)
    {
        ImUser imUser = L2ApplicationContext.getInstance().getImUserFromContext(uid);
        if(imUser == null){
            TraceLogger.trace(Constant.MESSAGE_TRACE,"while push message {}, user {} not login, no message to push", imMessage.getMid(), uid);
            return;
        }
        Map<String, ImSession> userSessions = imUser.getSessions();
        if (userSessions == null || userSessions.size() == 0) {
            TraceLogger.trace(Constant.MESSAGE_TRACE,"while push message {}, user {} session is empty, no message to push", imMessage.getMid(), uid);
            return;
        }
        for (ImSession session : userSessions.values()) {
            session.pushMsg(imMessage);
            //根据消息的接收者  将消息分发到任务处理线程的特定线程上
            L2ApplicationContext.getInstance().pushThreadPool.addPushTask(session);
        }
    }
}
