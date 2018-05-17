package com.moer.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaoxuejian on 2018/5/7.
 * 群聊模型
 */
public class ImGroup {
    private int gid;
    private String groupName;
    /**
     * 直播间在线成员集合 会把消息推送给这个集合的所有成员
     */
    private Map<Integer, ImUser> userList = new HashMap<Integer, ImUser>();

    /**
     * 直播间黑名单成员集合  在黑名单中的成员不会给他推送消息
     */
    private Map<Integer, ImUser> blackList = new HashMap<Integer, ImUser>();

    /**
     * 直播間最新的一條消息
     */
    private ImMessage lastestMsg;

}
