package com.moer.l2;

import com.alibaba.fastjson.JSON;
import com.moer.bean.GroupMembers;
import com.moer.config.ImConfig;
import com.moer.config.NettyConfig;
import com.moer.entity.ImGroup;
import com.moer.entity.ImSession;
import com.moer.entity.ImUser;
import com.moer.service.GroupInfoService;
import com.moer.service.GroupMembersService;
import com.moer.service.ServiceFactory;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    public Map<Integer, ImGroup> IMGroupContext = new ConcurrentHashMap<>();
    public TimerThread timerThread = new TimerThread();
    public ImConfig imConfig;
    public NettyConfig nettyConfig;

    public void addOnlineUserSession(int uid, ImSession imSession) {
        ImUser imUser = IMUserContext.get(uid);
        Map<String, ImSession> sessionMap = null;
        if (imUser == null) {
            imUser = new ImUser();
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
        //将session加入在线集合
        addOnlineUserSession(imSession.getUid(),imSession);
        //更新Group的信息
        GroupMembersService membersService = ServiceFactory.getInstace(GroupMembersService.class);
        GroupMembers members = new GroupMembers();
        members.setUid(imSession.getUid());
        List<GroupMembers> membersList =  membersService.getMember(members);
        if (membersList != null && membersList.size() > 0) {
            GroupInfoService groupService = ServiceFactory.getInstace(GroupInfoService.class);
            for (GroupMembers item: membersList) {
                //更新直播间群组的在线人数
                groupService.incrOnlineNum(item.getGid(), 1);
                //更新ImGroup里面的在线用户集合
                ImGroup imGroup = IMGroupContext.get(item.getGid());
                if (imGroup == null) {
                    imGroup = ImGroup.initImGroup(groupService.getById(Integer.valueOf(item.getGid())));
                }
                imGroup.userList.put(item.getUid(),item);
                //将直播间加入用户订阅的群聊列表
                IMUserContext.get(item.getUid()).getGroupMap().put(Integer.valueOf(item.getGid()),imGroup);

            }
        }
    }

    /**
     * 注销用户 清理用户在线数据
     * @TODO
     */
    public void logout(ImSession imSession,String message)
    {
        if (imSession == null)
            return;
        //清理用户所在直播间的数据
        ImUser imUser= IMUserContext.get(imSession.getUid());
        if (imUser == null) return;
        Map<Integer,ImGroup> userGroup = imUser.getGroupMap();
        if (userGroup != null && userGroup.size() > 0) {
            GroupInfoService infoService = ServiceFactory.getInstace(GroupInfoService.class);
            for (Map.Entry<Integer,ImGroup> entry: userGroup.entrySet()) {
                //在线人数 减1
                infoService.incrOnlineNum(String.valueOf(entry.getValue().gid), -1);
                //群组在线集合 移除
                IMGroupContext.get(entry.getValue().gid).userList.remove(imSession.getUid());
            }
            //移除用户订阅的群组
            imUser.getGroupMap().clear();

        }
        Channel channel = imSession.getChannel();
        if (channel.isActive()) {
            Map<String, Object> map = new HashMap<>();
            map.put("code", 1000);
            map.put("message", "user logout");
            map.put("data", message);
            sendResponse(channel,JSON.toJSONString(map));
        }
        //移除用户在线session
        delOnlineUserSession(imSession);

    }

    public void sendResponse(Channel channel, String msg)
    {
        try {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.OK, Unpooled.wrappedBuffer(msg.getBytes("UTF-8")));
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            channel.write(response);
            channel.flush();
        }catch (Exception e){

        }


    }
}
