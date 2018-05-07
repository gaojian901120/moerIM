package com.moer.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gaoxuejian on 2018/5/7.
 * 用户相关的信息
 * 一个用户
 * 一个用户可能会有多个session
 */
public class ImUser
{
    int uid;

    /**
     * 用户当前 对应连接的session
     */
    List<ImSession> sessions;

    /**
     * 用户的未读消息数
     */
    int unReadMsgNum = 0;

    /**
     * 未读消息数详情
     */
    Map<String,String> unReadDetail = new HashMap<>();
}
