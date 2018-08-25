package com.moer.entity;

import com.moer.bean.GroupHistoryMessageWithBLOBs;

/**
 * Created by gaoxuejian on 2018/5/2.
 * 消息实体  保存一个消息的数据结构
 */
public class ImMessage extends GroupHistoryMessageWithBLOBs implements Comparable<ImMessage> {
    @Override
    public int compareTo(ImMessage o) {
        if (this.getSendTime() >= o.getSendTime()) {
            return 1;
        }else if (this.getSendTime() < o.getSendTime()) {
            return -1;
        }
        return 0;
    }
}
