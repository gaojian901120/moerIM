package com.moer.store;

import com.moer.entity.ImMessage;
import io.netty.channel.Channel;
import io.netty.util.internal.ConcurrentSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by gaoxuejian on 2018/5/3.
 */
public class UserStore {
    /**
     * 在线用户的消息队列
     * key为用户uid  value为Queue 保存该用户所有待push到客户端的消息
     */
    private ConcurrentMap<Integer, Queue<ImMessage>> userMessageMap = new ConcurrentHashMap();
    /**
     * @TODO 用户连接管理
     * key为用户uid
     * value为map map的key为sessionid value为用户连接  注意连接可能失效，比如客户端主动关闭了请求
     * sessionid和连接是一一对应的关系
     * 目前简单测试uid和channel对应
     */
    private ConcurrentMap<Integer, Channel> userChannelMap = new ConcurrentHashMap();
    /**
     * 所有在线用户的uid
     */
    private Set<Integer> onlineUser = new ConcurrentSet<>();

    /**
     * 用户添加到在线用户集合
     *
     * @param uid
     * @return
     */
    public boolean addOnlineUser(int uid) {
        return onlineUser.add(uid);
    }

    public boolean delOnlineUser(int uid) {
        return onlineUser.remove(uid);
    }

    /**
     * 获取所有的在线用户集合
     *
     * @return
     */
    public Set<Integer> getAllOnlineUser() {
        return onlineUser;
    }

    /**
     * 向用户在线消息队列push一条消息
     *
     * @param uid
     * @param imMessages
     * @return
     */
    public boolean pushMessage(int uid, List<ImMessage> imMessages) {
        Queue<ImMessage> queue = userMessageMap.get(uid);
        if (queue == null) {
            queue = new ConcurrentLinkedDeque<>();
            Queue<ImMessage> oldQueue = userMessageMap.putIfAbsent(uid, queue);
            if (oldQueue != null) {
                queue = oldQueue;
            }
        }
        return queue.addAll(imMessages);
    }

    /**
     * 弹出所有消息返回给客户端用户
     *
     * @param uid
     * @return
     */
    public List<ImMessage> popAllMessage(int uid) {
        Queue<ImMessage> queue = userMessageMap.get(uid);
        if (queue == null) {
            return null;
        }
        List<ImMessage> returnQ = new ArrayList<>();
        for (ImMessage im : queue) {
            returnQ.add(im);
            queue.remove(im);
        }
        return returnQ;
    }

    public Channel addChannel(int uid, Channel channel) {
        return userChannelMap.put(uid, channel);
    }

    public Channel delChannel(int uid, Channel channel) {
        return userChannelMap.remove(uid);
    }

    public Channel getChannel(int uid) {
        return userChannelMap.get(uid);
    }
}
