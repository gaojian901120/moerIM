package com.moer.handler;

import com.alibaba.fastjson.JSON;
import com.moer.L2ApplicationContext;
import com.moer.bean.GroupInfo;
import com.moer.bean.GroupMembers;
import com.moer.common.Constant;
import com.moer.common.ServiceFactory;
import com.moer.common.TraceLogger;
import com.moer.config.NettyConfig;
import com.moer.entity.ImGroup;
import com.moer.entity.ImMessage;
import com.moer.entity.ImSession;
import com.moer.entity.ImUser;
import com.moer.service.GroupInfoService;
import com.moer.service.GroupMembersService;
import com.moer.thread.DispatchServer;
import com.moer.zookeeper.NodeManager;
import com.moer.zookeeper.ServerNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPubSub;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * Created by gaoxuejian on 2018/5/3.
 * 消息数据流的入口 以mid为关键词进行查找
 */
public class RedisMessageHandler extends JedisPubSub {
    public static Logger logger = LoggerFactory.getLogger(RedisMessageHandler.class);

    public RedisMessageHandler() {
    }

    @Override
    public void onMessage(String channel, String message) {
        try {
            if (channel.equals(Constant.MSG_RECV_QUEUE)) {
                TraceLogger.trace(Constant.MESSAGE_TRACE,"redis channel {} receive message: {}",channel,message);
                Map<String,Object> messageMap = JSON.parseObject(message,Map.class);
                if(messageMap == null) return;
                ImMessage imMessage = new ImMessage();
                imMessage.setExtp(messageMap.get("extp").toString());
                imMessage.setShowType(Integer.valueOf(messageMap.get("show_type").toString()));
                imMessage.setChatType(Integer.valueOf(messageMap.get("chat_type").toString()));
                imMessage.setMid(messageMap.get("mid").toString());
                imMessage.setMsg(messageMap.get("msg").toString());
                imMessage.setMsgSeq(messageMap.containsKey("msg_seq") ? Integer.valueOf(messageMap.get("msg_seq").toString()) : -1);
                imMessage.setMsgType(Integer.valueOf(messageMap.get("msg_type").toString()));
                imMessage.setRecv(messageMap.get("recv").toString());
                imMessage.setSend(messageMap.get("send").toString());
                imMessage.setSendTime(Long.valueOf(messageMap.get("send_time").toString()));
                String  extp = imMessage.getExtp();
                int priority = 5;
                if (extp != null && !extp .equals("")){
                    Map<String,Object> extMap = (Map<String,Object>)JSON.parseObject(extp);
                    if (extMap.containsKey("priority")) {
                        priority = Integer.valueOf(extMap.get("priority").toString());
                    }
                }
                MessageDispatchHandler handler = new MessageDispatchHandler(priority, imMessage);
                DispatchServer.dispatchMsg(handler);
            }else if (channel.equals(Constant.DATA_SYNC_QUEUE)) {
                TraceLogger.trace(Constant.MEMBER_TRACE,"redis channel {} receive message: {}",channel,message);
                Map<String,Object> event = (Map<String, Object>) JSON.parseObject(message);
                if(event == null) return;
                String tableName = (String) event.get("tableName");
                String action = (String)event.get("action");
                if(action == null) return;
                Map<String,Object>  data = (Map<String, Object>)event.get("data");
                if ("group_info".equals(tableName)  && data.containsKey("gid")) {
                    long gid = Long.valueOf(data.get("gid").toString());
                    if(action.equals("delete")){
                        L2ApplicationContext.getInstance().removeImGroupInContext(String.valueOf(gid));
                    }else if (action.equals("update")) {
                        L2ApplicationContext.getInstance().updateGroupInfoInContext(String.valueOf(gid));
                    }
                }else if ("group_members".equals(tableName)) {
                    if (action.equals("update") || action.equals("add")) {
                        handleGroupMemberUpdate(event);
                    }else if (action.equals("delete")) {
                        handleGroupMemberDelete(event);
                    }
                }else if("m_user_black".equals(tableName)){
                    int uid = Integer.valueOf(data.get("uid").toString());
                    int blackUid = Integer.valueOf(data.get("black_uid").toString());
                    NodeManager nodeManager = NodeManager.getInstance();
                    ServerNode serverNode = nodeManager.getServerNode(blackUid);
                    NettyConfig nettyConfig = L2ApplicationContext.getInstance().nettyConfig;
                    if (!(serverNode != null && serverNode.getHost().equals(nettyConfig.getHostName()) && serverNode.getPort() == nettyConfig.getPort())) {
                        return;
                    }
                    if (action.equals("add")){
                        L2ApplicationContext.getInstance().addBlackContextRelationship(uid,blackUid);
                    }else if(action.equals("delete")){
                        L2ApplicationContext.getInstance().delBlackContextRelationship(uid,blackUid);
                    }
                }
            }
        }catch (Exception e){
            logger.warn("redis message {} in channel {} handle error: {}", JSON.toJSONString(message), channel, e.getMessage());
            logger.error(e.getMessage(),e);
        }

    }

    public void handleGroupMemberUpdate(Map<String,Object> event)
    {
        Map<String,Object> data = (Map<String, Object>) event.get("data");
        if(!data.containsKey("gid") || !data.containsKey("uid")){
            return;
        }
        long gid = Long.valueOf(data.get("gid").toString());
        int uid = Integer.valueOf(data.get("uid").toString());
        NodeManager nodeManager = NodeManager.getInstance();
        ServerNode serverNode = nodeManager.getServerNode(uid);
        NettyConfig nettyConfig = L2ApplicationContext.getInstance().nettyConfig;
        if (!(serverNode != null && serverNode.getHost().equals(nettyConfig.getHostName()) && serverNode.getPort() == nettyConfig.getPort())) {
            return;
        }
        //如果用戶不在线也不更新
        Map<String, ImSession> sessionMap = L2ApplicationContext.getInstance().getUserOnlineSession(uid);
        if(sessionMap == null || sessionMap.size() == 0){
            return;
        }
        ImGroup imGroup = L2ApplicationContext.getInstance().getImGroupInContext(String.valueOf(gid));
        GroupInfoService infoService = ServiceFactory.getInstace(GroupInfoService.class);
        GroupInfo groupInfo= infoService.getByGid(String.valueOf(gid));
        if(imGroup == null) {
            if (groupInfo == null) {
                return;
            } else {
                imGroup = L2ApplicationContext.getInstance().initImGroup(groupInfo);
            }
        }else {
            if(groupInfo == null){
                L2ApplicationContext.getInstance().removeImGroupInContext(String.valueOf(gid));
            }else {
                imGroup.groupInfo = groupInfo;
            }
        }
        Map<Integer, GroupMembers> membersMap = imGroup.getUserList();
        if (membersMap == null || !membersMap.containsKey(uid)){
            GroupMembersService membersService = ServiceFactory.getInstace(GroupMembersService.class);
            GroupMembers record = new GroupMembers();
            record.setGid(String.valueOf(gid));
            record.setUid(uid);
            List<GroupMembers> membersList = membersService.getMember(record);
            if (membersList != null && membersList.size()>0) {
                imGroup.addUser(uid,membersList.get(0));
                TraceLogger.trace(Constant.MEMBER_TRACE, "handleGroupMemberUpdate {} with gid {} ", uid, gid);
            }
        }
        GroupMembers groupMembers = membersMap.get(uid);
        if (event.containsKey("expire_time")) {
            try {
                groupMembers.setExpireTime(new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").parse(event.get("expire_time").toString()));
            }catch (Exception e){}
        }
        if (event.containsKey("role_flag")) {
            groupMembers.setRoleFlag(Integer.valueOf(event.get("role_flag").toString()));
        }
        ImUser imUser = L2ApplicationContext.getInstance().getImUserFromContext(uid);
        if(imUser!= null){
            imUser.addGroup(groupMembers.getGid(),groupMembers);
        }
    }

    public void handleGroupMemberDelete(Map<String,Object> event)
    {
        Map<String,Object> data = (Map<String, Object>) event.get("data");
        if(!data.containsKey("gid") || !data.containsKey("uid")){
            return;
        }
        String gid = data.get("gid").toString();
        int uid = Integer.valueOf(data.get("uid").toString());
        NodeManager nodeManager = NodeManager.getInstance();
        ServerNode serverNode = nodeManager.getServerNode(uid);
        NettyConfig nettyConfig = L2ApplicationContext.getInstance().nettyConfig;
        if (!(serverNode != null && serverNode.getHost().equals(nettyConfig.getHostName()) && serverNode.getPort() == nettyConfig.getPort())) {
            return;
        }
        ImGroup imGroup = L2ApplicationContext.getInstance().getImGroupInContext(gid);
        if(imGroup != null) {
            imGroup.removeUser(uid);
        }
        ImUser imUser = L2ApplicationContext.getInstance().getImUserFromContext(uid);
        if(imUser!= null){
            imUser.removeGroup(gid);
        }
    }
}
