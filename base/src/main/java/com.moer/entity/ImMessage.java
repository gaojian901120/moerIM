package com.moer.entity;

import com.moer.bean.GroupHistoryMessageWithBLOBs;

/**
 * Created by gaoxuejian on 2018/5/2.
 * 消息实体  保存一个消息的数据结构
 */
public class ImMessage extends GroupHistoryMessageWithBLOBs implements Comparable<ImMessage> {
    @Override
    public int compareTo(ImMessage o) {
        return Integer.valueOf(Long.valueOf(this.getSendTime() - o.getSendTime()).toString());
    }
}
