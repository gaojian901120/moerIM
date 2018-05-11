package com.moer.entity;

/**
 * Created by gaoxuejian on 2018/5/2.
 * 消息实体  保存一个消息的数据结构
 */
public class ImMessage {
    //消息mid 递增
    private long mid;
    private int send;
    private int recv;
    private String content;
    /**
     * 消息类型 文本 图片 视频等
     */
    private int msgType;
    private long sendTime;
    /**
     * 消息類型 發送到群聊、單聊等
     */
    private int chatType;
    private int extend;

    public int getSend() {
        return send;
    }

    public void setSend(int send) {
        this.send = send;
    }

    public int getRecv() {
        return recv;
    }

    public void setRecv(int recv) {
        this.recv = recv;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }
}
