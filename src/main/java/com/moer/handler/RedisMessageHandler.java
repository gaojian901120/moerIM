package com.moer.handler;

import com.alibaba.fastjson.JSON;
import com.moer.bean.GroupInfo;
import com.moer.bean.GroupMembers;
import com.moer.common.Constant;
import com.moer.dao.mysql.GroupMembersMapper;
import com.moer.entity.ImGroup;
import com.moer.entity.ImMessage;
import com.moer.l2.L2ApplicationContext;
import com.moer.server.DispatchServer;
import com.moer.service.GroupInfoService;
import com.moer.service.ServiceFactory;
import redis.clients.jedis.JedisPubSub;

import java.text.SimpleDateFormat;
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
        if (channel.equals(Constant.MSG_RECV_QUEUE)) {
            ImMessage imMessage = JSON.parseObject(message, ImMessage.class);
            String  extp = imMessage.getExtp();
            int priority = 5;
            if (extp != null && extp != ""){
                Map<String,Object> extMap = (Map<String,Object>)JSON.parseObject(extp);
                if (extMap.containsKey("priority")) {
                    priority = Integer.valueOf(extMap.get("priority").toString());
                }
            }
            MessageDispatchHandler handler = new MessageDispatchHandler(priority, imMessage);
            DispatchServer.dispatchMsg(handler);
        }else if (channel.equals(Constant.DATA_SYNC_QUEUE)) {
            Map<String,Object> event = (Map<String, Object>) JSON.parse(message);
            String tableName = (String) event.get("tableName");
            String action = (String)event.get("action");
            Map<String,Object>  data = (Map<String, Object>)event.get("data");
            if ("group_info".equals(tableName)  && data.containsKey("gid")) {
                long gid = Long.valueOf(data.get("gid").toString());
                if(action.equals("delete")){
                    L2ApplicationContext.getInstance().IMGroupContext.get(gid).groupInfo = null;
                }else if (action.equals("update")) {
                    ImGroup imGroup = L2ApplicationContext.getInstance().IMGroupContext.get(gid);
                    GroupInfoService infoService = ServiceFactory.getInstace(GroupInfoService.class);
                    GroupInfo groupInfo = infoService.getByGid(String.valueOf(gid));
                    imGroup.groupInfo = groupInfo;
                    L2ApplicationContext.getInstance().IMGroupContext.put(String.valueOf(gid), imGroup);
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
                    L2ApplicationContext.getInstance().IMUserContext.get(uid).userBlackList.put(blackUid,1);
                }else if(action.equals("delete")){
                    L2ApplicationContext.getInstance().IMUserContext.get(uid).userBlackList.remove(blackUid);
                }
            }
        }
    }

    public void handleGroupMemberUpdate(Map<String,Object> event)
    {
        long gid = Long.valueOf(event.get("gid").toString());
        int uid = Integer.valueOf(event.get("uid").toString());
        ImGroup imGroup = L2ApplicationContext.getInstance().IMGroupContext.get(gid);
        GroupInfo groupInfo = imGroup.groupInfo;
        if (groupInfo == null){
            GroupInfoService infoService = ServiceFactory.getInstace(GroupInfoService.class);
            groupInfo= infoService.getByGid(String.valueOf(gid));
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

    public void handleGroupMemberDelete(Map<String,Object> event)
    {
        int gid = Integer.valueOf(event.get("gid").toString());
        int uid = Integer.valueOf(event.get("uid").toString());
        ImGroup imGroup = L2ApplicationContext.getInstance().IMGroupContext.get(gid);
        imGroup.userList.remove(String.valueOf(uid));
    }
}
