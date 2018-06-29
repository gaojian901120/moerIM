package com.moer.entity;

import com.moer.bean.GroupInfo;
import com.moer.bean.GroupMembers;

import java.util.*;
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
   Queue<ImMessage> unReadDetail = new LinkedList<>();

    /**
     * 用户订阅的群聊列表
     */
    Map<Integer, ImGroup> groupMap = new HashMap<Integer, ImGroup>();

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

    public Map<Integer, ImGroup> getGroupMap() {
        return groupMap;
    }

    public void setGroupMap(Map<Integer, ImGroup> groupMap) {
        this.groupMap = groupMap;
    }

    public void newUnreadMsg(ImMessage imMessage)
    {
        unReadMsgNum++;
        unReadDetail.add(imMessage);
        if (unReadDetail.size() > 20){
            unReadDetail.poll();
        }

    }
}
