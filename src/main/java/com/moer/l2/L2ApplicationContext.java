package com.moer.l2;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.moer.bean.GroupInfo;
import com.moer.bean.GroupMembers;
import com.moer.bean.UserBlack;
import com.moer.common.Constant;
import com.moer.config.ImConfig;
import com.moer.config.NettyConfig;
import com.moer.entity.ImGroup;
import com.moer.entity.ImMessage;
import com.moer.entity.ImSession;
import com.moer.entity.ImUser;
import com.moer.redis.RedisStore;
import com.moer.service.GroupInfoService;
import com.moer.service.GroupMembersService;
import com.moer.common.ServiceFactory;
import com.moer.service.UserBlackService;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;
import io.netty.util.internal.ConcurrentSet;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

    public Map<Integer, ImUser> IMUserContext = new ConcurrentHashMap<>();
    public Map<String, ImGroup> IMGroupContext = new ConcurrentHashMap<>();
    public Map<Integer,Set<Integer>> UserBlackContext = new ConcurrentHashMap<>();
    public TimerThread timerThread = new TimerThread();
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
    }

    /**
     * @param imSession
     * @return true 表示用户不在线 false 表示用户在线
     */
    public boolean delOnlineUserSession(ImSession imSession) {
        Map<String, String> map = imSession.decodeSessionId(imSession.getSeeesionId());
        int uid = Integer.valueOf(map.get("uid"));
        ImUser imUser = IMUserContext.get(uid);
        if (imUser != null) {
            Map<String, ImSession> sessionMap = imUser.getSessions();
            if (sessionMap!=null && sessionMap.size() > 0) {
                sessionMap.remove(imSession.getSeeesionId());
            }
            if(sessionMap == null || sessionMap.size() == 0){
                IMUserContext.remove(uid);
            }
        }
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
    public void login(ImSession imSession)
    {
        System.out.println("user login :" + imSession.getUid());
        //将session加入在线集合 以及初始化ImUser用户
        addOnlineUserSession(imSession.getUid(),imSession);
        //更新Group的信息
        Map<String,GroupMembers> memberMap = IMUserContext.get(imSession.getUid()).getGroupMap();

        if (memberMap != null && memberMap.size() > 0) {
            GroupInfoService groupService = getInstace(GroupInfoService.class);
            for (Map.Entry<String,GroupMembers> item: memberMap.entrySet()) {
                //更新直播间群组的在线人数
                groupService.incrOnlineNum(item.getKey(), 1);
                //更新ImGroup里面的在线用户集合
                ImGroup imGroup = IMGroupContext.get(item.getKey());
                if (imGroup == null) {
                    GroupInfo group = groupService.getByGid(item.getKey());
                    if (group == null) {
                        continue;
                    }
                    imGroup = L2ApplicationContext.getInstance().initImGroup(groupService.getByGid(item.getKey()));
                    IMGroupContext.put(item.getKey(), imGroup);
                }
                imGroup.addUser(item.getValue().getUid(),item.getValue());
            }
        }
    }

    /**
     * 注销用户 清理用户在线数据
     * @TODO
     */
    public void logout(ImSession imSession,String message)
    {
        logout(imSession,message,1000);
    }
    public void logout(ImSession imSession,String message, int code){
        System.out.println("logout------------:" + imSession.getUid() + ", message:" + message);
        if (imSession == null)
            return;
        //清理用户所在直播间的数据
        ImUser imUser= IMUserContext.get(imSession.getUid());
        if (imUser == null) return;
        Map<String,GroupMembers> userGroup = imUser.getGroupMap();
        if (userGroup != null && userGroup.size() > 0) {
            GroupInfoService infoService = getInstace(GroupInfoService.class);
            for (Map.Entry<String,GroupMembers> entry: userGroup.entrySet()) {
                String key = entry.getKey();
                //在线人数 减1
                infoService.incrOnlineNum(String.valueOf(entry.getKey()), -1);
                //群组在线集合 移除
                if(IMGroupContext.containsKey(key)){
                    IMGroupContext.get(key).remove(imSession.getUid());
                }
            }
            //移除用户订阅的群组
            imUser.getGroupMap().clear();

        }
        Channel channel = imSession.getChannel();
        if (channel.isActive()) {
            Map<String, Object> map = new HashMap<>();
            map.put("code", code);
            map.put("message", "user logout");
            map.put("data", message);
            sendResponse(channel,JSON.toJSONString(map));
        }
        //移除用户在线session
        delOnlineUserSession(imSession);
        RedisStore redis = ServiceFactory.getRedis();
        redis.hdel(Constant.REDIS_USER_STATUS+imSession.getUid(),Constant.REDIS_USER_STATUS_FIELD_ONLINE);
        redis.hdel(Constant.REDIS_USER_ONLINE_SET, imSession.getUid() + "");
    }
    public void sendResponse(Channel channel, String msg)
    {
        try {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.OK, Unpooled.wrappedBuffer(msg.getBytes("UTF-8")));
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            channel.writeAndFlush(response);
        }catch (Exception e){

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
        Map<String,GroupMembers> memberMap = new HashMap<>();
        if (membersList != null && membersList.size() > 0) {
            for (GroupMembers member : membersList){
                memberMap.put(member.getGid(),member);
            }
            user.setGroupMap(memberMap);
        }
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
