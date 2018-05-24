package com.moer.l2.context;

import com.moer.entity.ImSession;

import java.util.List;
import java.util.Map;
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

    public void addOnlineUser(int uid, ImSession imSession)
    {
        Map<String,ImSession> sessionMap = onlineUserMap.get(uid);
        if (sessionMap == null) {
            sessionMap = new ConcurrentHashMap<>();
            Map<String, ImSession> oldMap = onlineUserMap.putIfAbsent(uid,sessionMap);
            if (oldMap!=null) sessionMap = oldMap;
        }
        sessionMap.put(imSession.)
    }
}
