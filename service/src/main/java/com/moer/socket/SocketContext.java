package com.moer.socket;

import com.moer.entity.ImMessage;
import io.netty.channel.ChannelHandlerContext;

import java.util.Vector;

//保存单个长链接的上下文数据
public class SocketContext {
    private static final int EXPIRE_THRESHOLD = 120*1000;
    private Integer uid;

    private String channelId;

    private ChannelHandlerContext channelContext;
    //直接使用netty提供的心跳检测机制，不会再使用单独定义的线程进行服务端的心跳检测
    private volatile Long lastHeartBeatTime;

    private Short status;
    //保存待下发的消息，如果连接不断，那么
    private Vector<ImMessage> msgQueue = new Vector<>();

    //记录客户端传递的设备id  用于区分是不是一个设备传递过来的
    private String deviceToken;

    public boolean valid(){
        long cur = System.currentTimeMillis();
        if(cur - lastHeartBeatTime > EXPIRE_THRESHOLD){
            return false;
        }
        //@TODO channel没有激活 说明连接中断 需要处理
        if(!channelContext.channel().isActive()){
            return false;
        }
        return true;
    }

    public Integer getUid() {
        return uid;
    }

    public String getChannelId() {
        return channelId;
    }

    public ChannelHandlerContext getChannelContext() {
        return channelContext;
    }

    public Long getLastHeartBeatTime() {
        return lastHeartBeatTime;
    }

    public Short getStatus() {
        return status;
    }

    public Vector<ImMessage> getMsgQueue() {
        return msgQueue;
    }

    public void setLastHeartBeatTime(Long lastHeartBeatTime) {
        this.lastHeartBeatTime = lastHeartBeatTime;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }
}
