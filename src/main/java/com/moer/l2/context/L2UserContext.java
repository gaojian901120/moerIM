package com.moer.l2.context;

import com.moer.entity.ImSession;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by gaoxuejian on 2018/5/19.
 * 用户上下文数据 包括所有的在线用户
 */
public class L2UserContext {
    /**
     * 在线用户集合，key为用户uid value为用户的Imsession结构体的Map 当value为空说明用户已经不在线了
     */
    private Map<Integer, Map<String,ImSession>> onlineUserMap = new ConcurrentHashMap<>();

    /**
     * 用户加入的所有的群组
     */
    private Map<Integer, Map> onlineUserGroupMap = new ConcurrentHashMap<>();

    public void addOnlineUserSession(int uid, ImSession imSession)
    {
        Map<String,ImSession> sessionMap = onlineUserMap.get(uid);
        if (sessionMap == null) {
            sessionMap = new ConcurrentHashMap<>();
            Map<String, ImSession> oldMap = onlineUserMap.putIfAbsent(uid,sessionMap);
            if (oldMap!=null) sessionMap = oldMap;
        }
        sessionMap.put(imSession.getSeeesionId(), imSession);
    }

    /**
     *
     * @param imSession
     * @return true 表示用户不在线 false 表示用户在线
     */
    public boolean delOnlineUserSession(ImSession imSession)
    {
        Map<String, String> map = imSession.decodeSessionId(imSession.getSeeesionId());
        int uid = Integer.valueOf(map.get("uid"));
        Map<String,ImSession> sessionMap = onlineUserMap.get(uid);
        sessionMap.remove(imSession.getSeeesionId());
        if (sessionMap.size() == 0) {
            return true;
        }
        return false;
    }

    public Map<String,ImSession> getUserOnlineSession(Integer uid)
    {
        return onlineUserMap.get(uid);

    }
}
