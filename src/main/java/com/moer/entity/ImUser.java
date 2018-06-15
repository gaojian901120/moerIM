package com.moer.entity;

import com.moer.bean.GroupInfo;
import com.moer.bean.GroupMembers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by gaoxuejian on 2018/5/7.
 * 用户相关的信息
 * 一个用户
 * 一个用户可能会有多个session
 */
public class ImUser {
    int uid;

    /**
     * 用户当前 对应连接的session
     * 一般一个用户同时最多几个连接 不需要太多考虑多线程读写该map的场景
     */
    private Map<String, ImSession> sessions =  new ConcurrentHashMap<>();

    /**
     * 用户的未读消息数
     */
    int unReadMsgNum = 0;

    /**
     * 未读消息数详情
     */
    Map<String, ImMessage> unReadDetail = new HashMap<String, ImMessage>();

    /**
     * 用户订阅的群聊列表
     */
    Map<Integer, GroupInfo> groupMap = new HashMap<Integer, GroupInfo>();

    public Map<String, ImSession> getSessions() {
        return sessions;
    }
}
