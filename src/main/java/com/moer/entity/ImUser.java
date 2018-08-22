package com.moer.entity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
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
     * 用户首次connect的时候会生成一个session，过期退出登录 进行清理
     */
    private Map<String, ImSession> sessions =  new ConcurrentHashMap<>();

    /**
     * 用户的未读消息数
     * 根据消息序列号进行计算
     * @TODO  更新用户未读消息数
     */
    int unReadMsgNum = 0;

    /**
     * 未读消息数详情
     */
   Queue<ImMessage> unReadDetail = new LinkedList<>();

    /**
     * 用户订阅的群聊列表
     */
    Map<String, ImGroup> groupMap = new HashMap<String, ImGroup>();

    public Map<String, ImSession> getSessions() {
        return sessions;
    }

    public void setSessions(Map<String, ImSession> sessions) {
        this.sessions = sessions;
    }

    public int getUnReadMsgNum() {
        return unReadMsgNum;
    }

    public void setUnReadMsgNum(int unReadMsgNum) {
        this.unReadMsgNum = unReadMsgNum;
    }

    public Queue<ImMessage> getUnReadDetail() {
        return unReadDetail;
    }

    public void setUnReadDetail(Queue<ImMessage> unReadDetail) {
        this.unReadDetail = unReadDetail;
    }

    public Map<String, ImGroup> getGroupMap() {
        return groupMap;
    }

    public void setGroupMap(Map<String, ImGroup> groupMap) {
        this.groupMap = groupMap;
    }

}
