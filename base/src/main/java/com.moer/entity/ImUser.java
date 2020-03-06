package com.moer.entity;

import com.moer.bean.GroupMembers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by gaoxuejian on 2018/5/7.
 * 用户相关的信息
 * 一个用户
 * 一个用户可能会有多个session
 */
public class ImUser {
    private int uid;

    /**
     * 用户当前 对应连接的session
     * 一般一个用户同时最多几个连接 不需要太多考虑多线程读写该map的场景
     * 用户首次connect的时候会生成一个session，过期退出登录 进行清理
     */
    private Map<String, ImSession> sessions =  new ConcurrentHashMap<String, ImSession>();



    /**
     * 用户订阅的群聊列表 以及在该群组中的身份说明
     */
    private Map<String, GroupMembers> groupMap = new HashMap<String, GroupMembers>();

    public Map<String, ImSession> getSessions() {
        return sessions;
    }

    public synchronized void addGroup(String gid,GroupMembers members){
        if(groupMap == null) {
            groupMap = new HashMap<>();
        }
        groupMap.put(gid,members);
    }
    public synchronized void removeGroup(String gid){
        if(groupMap!=null){
            groupMap.remove(gid);
        }
    }
    public Map<String, GroupMembers> getGroupMap() {
        return groupMap;
    }

    public void setGroupMap(Map<String, GroupMembers> groupMap) {
        this.groupMap = groupMap;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }
}
