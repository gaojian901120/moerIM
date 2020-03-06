package com.moer.entity;

import com.moer.util.CryptUtil;
import io.netty.channel.Channel;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
    public static final int SESSION_STATUS_UNPULL = -1;//-1表示该session刚通过connect调用创建 还没有通过pull进行链接
    public static final int SESSION_STATUS_PULLING = 0;//表示当前有pull请求关联在该session上 正处于
    public static final int SESSION_STATUS_EXPIRED = 2;//过期 主要是多端登陆的情况下 在同一端有多个session存在的话 在不允许开启多个页面的话 后链接的session会把先链接的session会话给标记成  这个状态的sessoin不再接受新的请求
    public static final String SESSION_SOURCE_WEB = "web";
    public static final String SESSION_SOURCE_APP = "app";
    public static final String FROM_IOS = "ios";
    public static final String FROM_ANDROID = "android";
    public static final String FROM_WEB = "web";
    public static final String USER_COOKIE_FIELD = "_jm_ppt_id";
    public static final int SESSION_FREELIVE_LIMIT =600000;//session空闲生存时间  30s  即30s内没有请求到来 则session会话过期
    public static final int MESSAGE_BLOCK_SIZE = 50;
    private Lock lock = new ReentrantLock();
    private Object msgLock = new Object();

    public Object getMsgLock() {
        return msgLock;
    }

    /**
     * 待推送到客户端的消息队列
     *多个线程同时写 需要并发 vector是线程安全的数据结构
     */
    Vector<ImMessage> msgQueue  = new Vector<>();
    Deque<Vector<ImMessage>> msgBlocks = new LinkedBlockingDeque<>();
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
     * 当用户http请求结束时 如果没有复用连接则下次请求会使用新的链接channelid会改变
     * 两者没有啥对用关系  一个channel上可能承载好几个会话 但这个是链接层面  比如app内 多次重新链接的情况下 以及keeplive情况下 就会使用同一个channel传递数据
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
    public volatile int status = SESSION_STATUS_UNPULL;

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
        lock.lock();
        this.status = status;
        lock.unlock();
    }


    public void pushMsg(ImMessage imMessage) {
        msgQueue.add(imMessage);
        if(msgQueue.size()>=MESSAGE_BLOCK_SIZE){
            synchronized (msgQueue){
                msgBlocks.add(msgQueue);
                msgQueue = new Vector<>();
            }
        }
    }
    public synchronized void cleanMsg(){
        for (Vector v : msgBlocks){
            v.clear();
        }
        msgBlocks.clear();
        msgQueue.clear();
    }
    public void addMsgBlockToHead(Vector<ImMessage> list){
        msgBlocks.addFirst(list);
    }
    public Vector<ImMessage> popMsgFromTail() {
        Vector<ImMessage> messages  = msgBlocks.pollFirst();
        if(messages == null){
            synchronized (msgQueue){
                messages = msgQueue;
                msgQueue = new Vector<>();
            }
        }
        return messages;

    }

    /**
     * sesion 是否有效 有效则不能够被timethread清理
     * 有效的定义：session = 0
     * @return
     */
    public boolean isVaild(){
        if(status == SESSION_STATUS_PULLING)
            return true;
        long curTime = System.currentTimeMillis();
        if(status == SESSION_STATUS_UNPULL){
            if(curTime - updateTime <=SESSION_FREELIVE_LIMIT){
                return true;
            }
        }
        return false;
    }

}
