package com.moer.entity;

import com.moer.bean.GroupInfo;
import com.moer.bean.GroupMembers;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaoxuejian on 2018/5/7.
 * 群聊模型
 */
public class ImGroup {
    private GroupInfo groupInfo;
    private int gid;
    /**
     * 直播间在线成员集合 会把消息推送给这个集合的所有成员
     */
    private Map<Integer, GroupMembers> userList = new HashMap<Integer, GroupMembers>();

    /**
     * 直播间黑名单成员集合  在黑名单中的成员不会给他推送消息  同时也不能发送消息
     */
    private Map<Integer, GroupMembers> blackList = new HashMap<Integer, GroupMembers>();

}
