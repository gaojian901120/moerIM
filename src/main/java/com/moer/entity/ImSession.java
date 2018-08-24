package com.moer.entity;

import com.moer.util.CryptUtil;
import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Created by gaoxuejian on 2018/5/4.
 * 会话管理
 * 一个channel等价于一个tcp层的连接
 * 一个channel可以对应多个多个http连接 看http返回字段的设置
 * session表示一次会话 在pc上看代码打开的一个直播间页面 打开多个直播间页面表示多个会话 通过这个来控制是否允许多端登录
 * 在连接直播间之前需要验证 包括是否登录，是否在黑名单，是否禁用等等，验证通过后生成一个sessionid 以及其他信息给用户
 * 在app上在每台设备上的表现同pc上，通过是否可以存在多个sessionid来控制是否可以在app上多设备登录。
 * sessionid理论上不失效，服务端定时检测所有的session，如果session对应的用户当前没有http请求hold 就表示失效了，会清除所有session相关的信息
 */
public class ImSession {
    public static final String sessionCode = "fgviunfkls8wemzdwen7q2";
    public static final int SESSION_STATUS_NORMAL = 1;//任一一个为1 则表示用户在线
    public static final int SESSION_STATUS_EXPIRE = 2;//过期 表示多端登陆 被踢掉的一个会话
    public static final String SESSION_SOURCE_WEB = "web";
    public static final String SESSION_SOURCE_APP = "app";
    /**
     * 待推送到客户端的消息队列
     *多个线程同时写 需要并发 vector是线程安全的数据结构
     */
    Vector<ImMessage> msgQueue  = new Vector<>();
    /**
     * 会话id  首次进入直播间验证通过后生成会话id
     */
    private String seeesionId;
    /**
     * 会话建立的时间
     */
    private long createTime;
    /**
     * 当前会话所对应的连接
     * 一个channel其实就是一个scoket  当一个 进程内没有断开的时候 可以被重复使用
     */
    private Channel channel;

    private long updateTime;

    /**
     * 会话对应的用户uid
     */
    private Integer uid;
    //从那个端连接上来的
    private String source;

    public Map<String, String> decodeSessionId(String seeesionId)
    {
        String decodeSessionId = CryptUtil.hexStr2Str(seeesionId);
        String[] decodeArr = decodeSessionId.split(sessionCode);
        Map<String, String> map = new HashMap<>();
        map.put("uid", decodeArr[0]);
        map.put("time", decodeArr[1]);
        return map;
    }

    public String getSource() {
        return source;
    }

    /**
     *
     * 默认-1 表示当前session还没有pull请求 hold
     * 0 表示有pull请求hold在服务端
     * 1表示session需要被删除
     */
    public int status = -1;

    public static String getSessionCode() {
        return sessionCode;
    }

    public String getSeeesionId() {
        return seeesionId;
    }

    public void setSeeesionId(String seeesionId) {
        this.seeesionId = seeesionId;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


    public void pushMsg(ImMessage imMessage) {
        msgQueue.add(imMessage);
    }
    public Vector<ImMessage> popAllMsgQueue() {
        Vector<ImMessage> messages = new Vector<>();
        messages.addAll(msgQueue);
        msgQueue.clear();
        return messages;
    }

}
