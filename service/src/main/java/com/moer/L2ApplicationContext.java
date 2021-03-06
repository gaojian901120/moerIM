package com.moer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.moer.bean.GroupInfo;
import com.moer.bean.GroupMembers;
import com.moer.bean.UserBlack;
import com.moer.common.Constant;
import com.moer.common.ServiceFactory;
import com.moer.common.TraceLogger;
import com.moer.config.ImConfig;
import com.moer.config.NettyConfig;
import com.moer.entity.ImGroup;
import com.moer.entity.ImMessage;
import com.moer.entity.ImSession;
import com.moer.entity.ImUser;
import com.moer.redis.RedisStore;
import com.moer.service.GroupInfoService;
import com.moer.service.GroupMembersService;
import com.moer.service.UserBlackService;
import com.moer.thread.*;
import com.moer.thread.TimerThread;
import io.netty.channel.Channel;
import io.netty.util.internal.ConcurrentSet;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static com.moer.common.ActionHandler.sendHttpResp;
import static com.moer.common.ServiceFactory.getInstace;

/**
 * Created by gaoxuejian on 2018/5/19.
 * 服务层节点的全局上下文
 * 主要存储业务逻辑上的数据
 */
public class L2ApplicationContext {
    private static class L2ApplicationContextHolder {
        private static final L2ApplicationContext context = new L2ApplicationContext();
    }

    public final static L2ApplicationContext getInstance() {
        return L2ApplicationContextHolder.context;
    }

    private L2ApplicationContext() {
    }

    public Map<String, ImGroup> getIMGroupContext() {
        return IMGroupContext;
    }
            BlockingQueue queue = new LinkedBlockingQueue();
    private Map<Integer, ImUser> IMUserContext = new ConcurrentHashMap<>();
    private Map<String, ImGroup> IMGroupContext = new ConcurrentHashMap<>();
    public Map<Integer,Set<Integer>> UserBlackContext = new ConcurrentHashMap<>();
    public TimerThread timerThread = new TimerThread();
    public DataSyncToRedisThread dataSyncToRedisThread = new DataSyncToRedisThread();
    public SubscribeThread subscribeThread = new SubscribeThread();
    public MonitorThread monitorThread = new MonitorThread();
    public PushThreadPool pushThreadPool = new PushThreadPool();
    public ImConfig imConfig;
    public NettyConfig nettyConfig;
    //添加黑名单
    public void addBlackContextRelationship (Integer uid, Integer blackUser){
        Set<Integer> blackSet = UserBlackContext.get(uid);
        if(blackSet == null){
            blackSet = new ConcurrentSet<>();
        }
        blackSet.add(blackUser);
        UserBlackContext.put(uid,blackSet);
    }
    public void delBlackContextRelationship(Integer uid, Integer blackUser){
        Set<Integer> blackSet = UserBlackContext.get(uid);
        if(blackSet == null){
            return;
        }
        blackSet.remove(blackUser);
        UserBlackContext.put(uid,blackSet);
    }
    public void addOnlineUserSession(int uid, ImSession imSession) {

        ImUser imUser = IMUserContext.get(uid);
        Map<String, ImSession> sessionMap = null;
        if (imUser == null) {
            imUser = initImUser(uid);
            ImUser oldUser = IMUserContext.putIfAbsent(uid,imUser);
            if (oldUser != null) imUser = oldUser;
        }
        sessionMap = imUser.getSessions();
        if (sessionMap == null) {
            sessionMap = new ConcurrentHashMap<>();
        }
        sessionMap.put(imSession.getSeeesionId(), imSession);
        IMUserContext.put(uid,imUser);
    }

    public ImUser getImUserFromContext(int uid){
        return IMUserContext.get(uid);
    }
    public  Map<Integer, ImUser> getIMUserContext(){
        return IMUserContext;
    }
    /**
     * @param imSession
     * @return true 表示用户不在线 false 表示用户在线
     */
    public boolean delOnlineUserSession(ImSession imSession) {
        int uid = imSession.getUid();
        ImUser imUser = IMUserContext.get(uid);
        if (imUser != null) {
            Map<String, ImSession> sessionMap = imUser.getSessions();
            if (sessionMap!=null && sessionMap.size() > 0) {
                sessionMap.remove(imSession.getSeeesionId());
            }
        }
        imSession.cleanMsg();
        return  true;
    }

    public Map<String, ImSession> getUserOnlineSession(Integer uid) {
        ImUser imUser = IMUserContext.get(uid);
        if (imUser != null) {
            return imUser.getSessions();
        }
        return null;
    }

    /**
     * 登陆 用户第一次调用connect方法的时候 需要初始化一些数据
     * @param imSession
     */
    public void sessionLogin(ImSession imSession)
    {
        int uid = imSession.getUid();
        if(uid == 104094517){
            System.out.println("debug");
        }
        //将session加入IMUserContext 用户上下文中
        TraceLogger.trace(Constant.MONITOR_TRACE, "addOnlineUserSession {} ", uid);

        addOnlineUserSession(imSession.getUid(),imSession);
        //更新Group的信息
        Map<String,GroupMembers> memberMap = IMUserContext.get(imSession.getUid()).getGroupMap();
        RedisStore redis = ServiceFactory.getRedis();
        if (memberMap != null && memberMap.size() > 0) {
            GroupInfoService groupService = getInstace(GroupInfoService.class);
            for (Map.Entry<String,GroupMembers> item: memberMap.entrySet()) {
                //更新直播间群组的在线人数
                groupService.incrOnlineNum(item.getKey(), 1);
                //更新ImGroup里面的在线用户集合
                ImGroup imGroup = getImGroupInContext(item.getKey());
                if(imGroup == null) {
                    continue;
                }
                imGroup.addUser(imSession.getUid(),item.getValue());
                //更新redis里面的群组的在线用户集合
                redis.sadd(Constant.REDIS_GROUP_SET_ONLINEUSER + item.getKey(),item.getValue().getUid()+"");
            }
        }
        //设置redis中用户为在线状态
        redis.hset(Constant.REDIS_USER_STATUS+imSession.getUid(),Constant.REDIS_USER_STATUS_FIELD_ONLINE,Constant.USER_ONLINE);
        //更新redis  添加用户到在线用户集合中
        redis.hset(Constant.REDIS_USER_ONLINE_SET, imSession.getUid()+"",System.currentTimeMillis()+"");
    }
    //初始化group 需要进行同步
    public  ImGroup getImGroupInContext(String gid){
        ImGroup imGroup = IMGroupContext.get(gid);
        if (imGroup == null) {
            synchronized(this) {
                imGroup = IMGroupContext.get(gid);
                if (imGroup == null) {
                    GroupInfoService groupService = getInstace(GroupInfoService.class);
                    System.out.println(gid + " is empty");
                    GroupInfo group = groupService.getByGid(gid);
                    if (group == null) {
                        return null;
                    }
                    imGroup = L2ApplicationContext.getInstance().initImGroup(group);
                    IMGroupContext.put(gid, imGroup);
                }
            }
        }
        return imGroup;
    }
    public void removeImGroupInContext(String gid){
        IMGroupContext.remove(gid);
    }

    public synchronized void updateGroupInfoInContext(String gid){
        ImGroup imGroup = getImGroupInContext(gid);
        if(imGroup == null) return;
        GroupInfoService infoService = ServiceFactory.getInstace(GroupInfoService.class);
        GroupInfo groupInfo = infoService.getByGid(String.valueOf(gid));
        if(groupInfo ==null){
            removeImGroupInContext(gid);
        }else {
            imGroup.groupInfo = groupInfo;
            IMGroupContext.put(gid, imGroup);
        }
    }

    /**
     * 注销用户 清理用户在线数据
     */
    public void sessionLogout(ImSession imSession,String message)
    {
        sessionLogout(imSession,message, Constant.CODE_UNCONNECT);
    }
    public void sessionLogout(ImSession imSession,String message, int code){
        if (imSession == null)
            return;
        //1、设置session 为过期状态 以防止timethread中的session检测再此执行该逻辑
        synchronized (imSession.getSeeesionId()) {
            try {
                if(imSession.getStatus() != ImSession.SESSION_STATUS_EXPIRED) {
                    imSession.setStatus(ImSession.SESSION_STATUS_EXPIRED);

                    //2、将该session 从对应用户的session list中移除掉
                    delOnlineUserSession(imSession);

                    //3、发送消息告知客户端该会话过期 原因可能是没有pull或者被踢出登陆
                    Channel channel = imSession.getChannel();
                    if (channel.isActive()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("code", code);
                        map.put("message", "user logout");
                        map.put("data", message);
                        String response = JSON.toJSONString(map);
                        if (imSession.getSource().equals(ImSession.SESSION_SOURCE_WEB)) {
                            response = "pullCallback(" + response + ")";
                        }
                        sendHttpResp(channel, response, false);
                    }else {
                        channel.close();
                    }
                    ImUser imUser = IMUserContext.get(imSession.getUid());
                    if (imUser == null) return;
                    Map<String, ImSession> sessionMap = imUser.getSessions();
                    //4、为空说明用户其他链接的会话了  这种情况下 需要清理用户的信息
                    RedisStore redis = ServiceFactory.getRedis();
                    if (sessionMap == null || sessionMap.size() == 0) {
                        //4.1 移除该用户的上下文信息
                        IMUserContext.remove(imSession.getUid());
                        //4.2清理用户所在直播间的数据
                        Map<String, GroupMembers> userGroup = imUser.getGroupMap();
                        if (userGroup != null && userGroup.size() > 0) {
                            GroupInfoService infoService = ServiceFactory.getInstace(GroupInfoService.class);
                            for (Map.Entry<String, GroupMembers> entry : userGroup.entrySet()) {
                                String key = entry.getKey();
                                //4.3群的在线人数 减1
                                infoService.incrOnlineNum(String.valueOf(entry.getKey()), -1);
                                //4.4群组在线人数集合 移除
                                if (IMGroupContext.containsKey(key)) {
                                    IMGroupContext.get(key).removeUser(imSession.getUid());
                                }
                                //移除群在线集合里面的redis uid
                                redis.srem(Constant.REDIS_GROUP_SET_ONLINEUSER + entry.getKey(), entry.getValue().getUid() + "");
                            }
                            //移除用户订阅的群组
                            imUser.getGroupMap().clear();

                        }
                        //4.5 redis中用户移除用户的在线状态
                        redis.hdel(Constant.REDIS_USER_STATUS + imSession.getUid(), Constant.REDIS_USER_STATUS_FIELD_ONLINE);
                        //4.6 redis 中全部用户在线集合中移除该用户
                        redis.hdel(Constant.REDIS_USER_ONLINE_SET, imSession.getUid() + "");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 该应用进程中保存的所有的直播间上下文的信息
     * @param groupInfo
     * @return
     */
    public ImGroup initImGroup(GroupInfo groupInfo)
    {
        UserBlackService userBlackService = ServiceFactory.getInstace(UserBlackService.class);
        List<UserBlack> userBlackList = userBlackService.getUserBlackList(groupInfo.getOwner());
        Set<Integer> blackMap = new ConcurrentSet<>();
        if(userBlackList!= null){
            for (UserBlack black:userBlackList) {
                if(black.getBlackUser() == null){
                    continue;
                }
                blackMap.add(black.getBlackUser());
            }
            UserBlackContext.put(groupInfo.getOwner(),blackMap);
        }
        ImGroup group = new ImGroup();
        group.groupInfo = groupInfo;
        group.gid = groupInfo.getGid();
        group.owner = groupInfo.getOwner();
        return group;
    }

    /**
     * 初始化用户 当用户信息在ImUserContext不存在的时候
     * @param uid
     * @return
     */
    public ImUser initImUser(Integer uid){
        ImUser user = new ImUser();
        user.setUid(uid);
        //加载用户订阅的所有的群
        GroupMembersService membersService = getInstace(GroupMembersService.class);
        GroupMembers members = new GroupMembers();
        members.setUid(uid);
        List<GroupMembers> membersList =  membersService.getMember(members);
        StringBuffer gidSb = new StringBuffer();
        Map<String,GroupMembers> memberMap = new HashMap<>();
        if (membersList != null && membersList.size() > 0) {
            for (GroupMembers member : membersList){
                memberMap.put(member.getGid(),member);
                gidSb.append(member.getGid());
                gidSb.append(",");
            }
            user.setGroupMap(memberMap);
        }
        TraceLogger.trace(Constant.MONITOR_TRACE, "initImUser {} with groupMembers {} ", uid, gidSb);

        return user;
    }

    public JSONArray convertMessage(List<ImMessage> imMessages){
        JSONArray array = new JSONArray();
        if(!Objects.isNull(imMessages)){
            for (ImMessage message: imMessages) {
                Map<String,Object> item = JSON.parseObject(JSON.toJSONString(message));
                item.put("extp",JSON.parse(message.getExtp()));
                if(message.getMsgType() != 1){
                    item.put("msg",JSON.parse(message.getMsg()));
                }
                array.add(item);
            }
        }
        return array;
    }
}
