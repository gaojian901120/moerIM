package com.moer.store;

import io.netty.util.internal.ConcurrentSet;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by gaoxuejian on 2018/5/3.
 * 当前节点的群组信息的查询及维护
 * 包括节点的在线用户集合，
 * 直播间的基本信息
 */
public class GroupStore {
    /**
     * 所有的在线直播间人数集合
     * key 为直播间id  value为Set 保存该直播间所有在线用户的集合
     */
    private ConcurrentMap<Integer, Set<Integer>> groupUserMap = new ConcurrentHashMap<>();


    /**
     * 将用户加入到直播间
     *
     * @param groupId
     * @param uid
     * @return
     */
    public boolean addGroupUser(int groupId, int uid) {
        Set<Integer> groupUsers = groupUserMap.get(groupId);
        if (groupUsers == null) {
            groupUsers = new ConcurrentSet<>();
            Set<Integer> oldGroup = groupUserMap.putIfAbsent(groupId, groupUsers);
            if (oldGroup != null) {
                groupUsers = oldGroup;
            }
        }
        return groupUsers.add(uid);
    }

    /**
     * 用户从直播间移除
     *
     * @param groupId
     * @param uid
     * @return
     */
    public boolean delGroupUser(int groupId, int uid) {
        Set<Integer> groupUsers = groupUserMap.get(groupId);
        if (groupUsers == null) {
            groupUsers = new ConcurrentSet<>();
            Set<Integer> oldGroup = groupUserMap.putIfAbsent(groupId, groupUsers);
            if (oldGroup != null) {
                groupUsers = oldGroup;
            }
        }
        return groupUsers.remove(uid);
    }

    /**
     * 获取某个直播间所有在线用户
     *
     * @return
     */
    public Set<Integer> getAllGroupOnlineUser(int groupId) {
        return groupUserMap.get(groupId);
    }


}
