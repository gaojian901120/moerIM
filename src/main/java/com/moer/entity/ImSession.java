package com.moer.entity;

import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;

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
    /**
     * 会话id  首次进入直播间验证通过后生成会话id
     */
    private String seeesionId;

    /**
     * 会话建立的时间
     */
    private String createTime;

    /**
     * 当前会话所对应的连接
     */
    private Channel channel;

    /**
     * 会话对应的用户uid
     */
    private Integer uid;

    /**
     * 验证当前会话有效性的token
     * token对应的生成规则=hash(sessionid+time+uid)
     */
    private String token;

    /**
     * 当前token是否失效
     * 如果第一次失效，会将字段设置为true，后续继续失效则不返回数据
     */
    private boolean tokenFailed = false;

    /**
     * 待推送到客户端的消息队列
     * @TODO arraylist or 其他列表结构
     */
    List<ImMessage> msgQueue = new ArrayList<>();
}
