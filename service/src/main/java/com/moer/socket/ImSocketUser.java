package com.moer.socket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ImSocketUser {
    private int uid;
    private Map<String, SocketContext> userSockets;
    //在线状态  1表示在线 0表示不在线
    private boolean online;
    //未读消息数
    private int unreadnum;

    public ImSocketUser(int uid ) {
        this.uid = uid;
        userSockets = new ConcurrentHashMap<>();
        online = false;
        unreadnum = 0;
    }

    public int getUid() {
        return uid;
    }

    public Map<String, SocketContext> getUserSockets() {
        return userSockets;
    }

    public boolean isOnline() {
        return online;
    }

    public int getUnreadnum() {
        return unreadnum;
    }


}
