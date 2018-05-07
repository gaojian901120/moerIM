package com.moer.queue;

/**
 * Created by gaoxuejian on 2018/5/2.
 * Im 队列 存储待发送消息队列
 * 待推送 消息队列 等
 */
public interface IMQueue {
    public <T> boolean push(String key, T t);

    public <T> T pop(String key);

    public int len(String key);
}
