package com.moer.l2;

import com.moer.config.ImConfig;
import com.moer.config.NettyConfig;
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

    public Map<Integer, ImUser> IMUserContext = new HashMap<>();
    public ImConfig imConfig;
    public NettyConfig nettyConfig;

    public void addOnlineUserSession(int uid, ImSession imSession) {
        ImUser imUser = IMUserContext.get(uid);
        Map<>
        if (imUser == null) {

        }
        if (sessionMap == null) {
            sessionMap = new ConcurrentHashMap<>();
            Map<String, ImSession> oldMap = sessions.putIfAbsent(uid, sessionMap);
            if (oldMap != null) sessionMap = oldMap;
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
        Map<String, ImSession> sessionMap = sessions.get(uid);
        sessionMap.remove(imSession.getSeeesionId());
        if (sessionMap.size() == 0) {
            return true;
        }
        return false;
    }

    public Map<String, ImSession> getUserOnlineSession(Integer uid) {
        return onlineUserMap.get(uid);
    }
}
