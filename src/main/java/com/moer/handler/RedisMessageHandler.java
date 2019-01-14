package com.moer.handler;

import com.alibaba.fastjson.JSON;
import com.moer.bean.GroupInfo;
import com.moer.bean.GroupMembers;
import com.moer.common.Constant;
import com.moer.common.ServiceFactory;
import com.moer.common.TraceLogger;
import com.moer.dao.mysql.GroupMembersMapper;
import com.moer.entity.ImGroup;
import com.moer.entity.ImMessage;
import com.moer.l2.DispatchServer;
import com.moer.l2.L2ApplicationContext;
import com.moer.service.GroupInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPubSub;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
                imMessage.setMsgSeq(Integer.valueOf(messageMap.get("msg_seq").toString()));
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
                Map<String,Object> event = (Map<String, Object>) JSON.parseObject(message);
                if(event == null) return;
                String tableName = (String) event.get("tableName");
                String action = (String)event.get("action");
                if(action == null) return;
                Map<String,Object>  data = (Map<String, Object>)event.get("data");
                if ("group_info".equals(tableName)  && data.containsKey("gid")) {
                    long gid = Long.valueOf(data.get("gid").toString());
                    if(action.equals("delete")){
                        L2ApplicationContext.getInstance().IMGroupContext.remove(String.valueOf(gid));
                    }else if (action.equals("update")) {
                        ImGroup imGroup = L2ApplicationContext.getInstance().IMGroupContext.get(String.valueOf(gid));
                        GroupInfoService infoService = ServiceFactory.getInstace(GroupInfoService.class);
                        GroupInfo groupInfo = infoService.getByGid(String.valueOf(gid));
                        if(groupInfo ==null){
                            L2ApplicationContext.getInstance().IMGroupContext.remove(String.valueOf(gid));
                        }else {
                            if(imGroup != null) {
                                imGroup.groupInfo = groupInfo;
                            }else {
                                imGroup = L2ApplicationContext.getInstance().initImGroup(groupInfo);
                            }
                            L2ApplicationContext.getInstance().IMGroupContext.put(String.valueOf(gid), imGroup);
                        }
                    }
                }else if ("group_members".equals(tableName)) {
                    if (action.equals("update") || action.equals("add")) {
                        handleGroupMemberUpdate(event);
                    }else if (action.equals("delete")) {
                        handleGroupMemberDelete(event);
                    }
                }else if("m_user_black".equals(tableName)){
                    int uid = Integer.valueOf(event.get("uid").toString());
                    int blackUid = Integer.valueOf(event.get("black_uid").toString());
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
        if(!event.containsKey("gid") || !event.containsKey("uid")){
            return;
        }
        long gid = Long.valueOf(event.get("gid").toString());
        int uid = Integer.valueOf(event.get("uid").toString());
        ImGroup imGroup = L2ApplicationContext.getInstance().IMGroupContext.get(String.valueOf(gid));
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
                L2ApplicationContext.getInstance().IMGroupContext.remove(String.valueOf(gid));
            }else {
                imGroup.groupInfo = groupInfo;
            }
        }
        Map<Integer, GroupMembers> membersMap = imGroup.getUserList();
        if (membersMap == null || !membersMap.containsKey(uid)){
            if (membersMap == null) {
                membersMap = new ConcurrentHashMap<>();
                imGroup.setUserList(membersMap);
            }
            GroupMembersMapper membersMapper = ServiceFactory.getInstace(GroupMembersMapper.class);
            GroupMembers record = new GroupMembers();
            record.setGid(String.valueOf(gid));
            record.setUid(uid);
            List<GroupMembers> membersList = membersMapper.selectBySelective(record);
            if (membersList != null) {
                membersMap.put(uid,membersList.get(0));
            }
        }
        //@TODO 业务监控程序 方便排查问题 并修复问题
        GroupMembers groupMembers = membersMap.get(uid);
        if (event.containsKey("expire_time")) {
            try {
                groupMembers.setExpireTime(new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").parse(event.get("expire_time").toString()));
            }catch (Exception e){}
        }
        if (event.containsKey("role_flag")) {
            groupMembers.setRoleFlag(Integer.valueOf(event.get("role_flag").toString()));
        }
    }

    public void handleGroupMemberDelete(Map<String,Object> event)
    {
        if(!event.containsKey("gid") || !event.containsKey("uid")){
            return;
        }
        int gid = Integer.valueOf(event.get("gid").toString());
        int uid = Integer.valueOf(event.get("uid").toString());
        ImGroup imGroup = L2ApplicationContext.getInstance().IMGroupContext.get(gid);
        if(imGroup != null) {
            imGroup.remove(uid);
        }
    }
}
