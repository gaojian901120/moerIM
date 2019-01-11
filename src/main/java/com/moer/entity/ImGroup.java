package com.moer.entity;

import com.moer.bean.GroupInfo;
import com.moer.bean.GroupMembers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by gaoxuejian on 2018/5/7.
 * 群聊模型
 */
public class ImGroup {
    public GroupInfo groupInfo;
    public String gid;
    public Integer owner;
    /**
     * 直播间在线成员集合 会把消息推送给这个集合的所有成员
     * 用户connect的时候更新在线用户列表 后续过期或者被人踢出去的时候 通过redis事件更新
     */
    private Map<Integer, GroupMembers> userList = new ConcurrentHashMap<>();

    public void addUser(Integer uid ,GroupMembers member){
        userList.put(uid, member);
    }

    public void remove(Integer uid){
        if(userList != null)
        userList.remove(uid);
    }

    public Map<Integer, GroupMembers> getUserList() {
        return userList;
    }

    public void setUserList(Map<Integer, GroupMembers> userList) {
        this.userList = userList;
    }
}
