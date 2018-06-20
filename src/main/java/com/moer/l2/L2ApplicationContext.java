package com.moer.l2;

import com.moer.config.ImConfig;
import com.moer.config.NettyConfig;
import com.moer.entity.ImGroup;
import com.moer.entity.ImSession;
import com.moer.entity.ImUser;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by gaoxuejian on 2018/5/19.
 * 服务层节点的全局上下文
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
}
