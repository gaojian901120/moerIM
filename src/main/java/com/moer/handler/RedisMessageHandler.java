package com.moer.handler;

import com.alibaba.fastjson.JSON;
import com.moer.bean.GroupInfo;
import com.moer.bean.GroupMembers;
import com.moer.common.Constant;
import com.moer.dao.mysql.GroupMembersMapper;
import com.moer.entity.ImGroup;
import com.moer.entity.ImMessage;
import com.moer.l2.L2ApplicationContext;
import com.moer.l2.L2ServiceApplication;
import com.moer.server.DispatchServer;
import com.moer.server.PushMessageServer;
import com.moer.service.GroupInfoService;
import com.moer.service.ServiceFactory;
import redis.clients.jedis.JedisPubSub;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by gaoxuejian on 2018/5/3.
 */
public class RedisMessageHandler extends JedisPubSub {

    public RedisMessageHandler() {
    }

    @Override
    public void onMessage(String channel, String message) {
        if (channel == Constant.MSG_RECV_QUEUE) {
            ImMessage imMessage = JSON.parseObject(message, ImMessage.class);
            MessageDispatchHandler handler = new MessageDispatchHandler(1, imMessage);
            DispatchServer.dispatchMsg(handler);
        }else if (channel == Constant.DATA_SYNC_QUEUE) {
            Map<String,Object> event = (Map<String, Object>) JSON.parse(message);
            String tableName = (String) event.get("tableName");
            String action = (String)event.get("action");
            if ("group_info".equals(tableName)) {
                int gid = Integer.valueOf(event.get("gid").toString());
                ImGroup imGroup = L2ApplicationContext.getInstance().IMGroupContext.get(gid);
                GroupInfo groupInfo = imGroup.groupInfo;
                if (groupInfo == null){
                    GroupInfoService infoService = ServiceFactory.getInstace(GroupInfoService.class);
                    groupInfo= infoService.getById(gid);
                    imGroup.groupInfo = groupInfo;
                }
                groupInfo.setTopic(event.get("topic").toString());
                groupInfo.setDescription(event.get("description").toString());
            }else if ("group_members".equals(tableName)) {
                int gid = Integer.valueOf(event.get("gid").toString());
                int uid = Integer.valueOf(event.get("uid").toString());
                ImGroup imGroup = L2ApplicationContext.getInstance().IMGroupContext.get(gid);
                GroupInfo groupInfo = imGroup.groupInfo;
                if (groupInfo == null){
                    GroupInfoService infoService = ServiceFactory.getInstace(GroupInfoService.class);
                    groupInfo= infoService.getById(gid);
                    imGroup.groupInfo = groupInfo;
                }
                Map<Integer, GroupMembers> membersMap = imGroup.userList;
                if (membersMap == null || !membersMap.containsKey(uid)){
                    if (membersMap == null) {
                        membersMap = new ConcurrentHashMap<>();
                        imGroup.userList = membersMap;
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
        }
    }
}
