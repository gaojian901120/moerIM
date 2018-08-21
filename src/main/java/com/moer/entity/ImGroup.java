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
    public int gid;
    /**
     * 直播间在线成员集合 会把消息推送给这个集合的所有成员
     * 用户connect的时候更新在线用户列表 后续过期或者被人踢出去的时候 通过redis事件更新
     */
    public Map<Integer, GroupMembers> userList = new ConcurrentHashMap<>();

    /**
     * 直播间黑名单成员集合  在黑名单中的成员不会给他推送消息  同时也不能发送消息
     * 用户connect的时候更新黑名单用户列表 后续过期或者被人踢出去的时候 通过redis事件更新
     */
    public Map<Integer, GroupMembers> blackList = new ConcurrentHashMap<Integer, GroupMembers>();

    /**
     * ImGroup 是GroupInfo的封装 实例化的时候必须指定一个groupInfo
     * @param groupInfo
     * @return
     */
    public static ImGroup initImGroup(GroupInfo groupInfo)
    {
        ImGroup group = new ImGroup();
        group.groupInfo = groupInfo;
        group.gid = groupInfo.getId();
        return group;
    }
}
