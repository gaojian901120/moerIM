package com.moer.zookeeper;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;

/**
 * Created by gaoxuejian on 2018/5/11.
 * zk监控的节点状态改变的时候 本节点需要做的工作
 */
public class NodeStatusMonitor implements Watcher, AsyncCallback.StatCallback
{
    public void processResult(int i, String s, Object o, Stat stat) {
        System.out.println("processResult");
    }

    public void process(WatchedEvent watchedEvent) {
        System.out.println("process");

    }
}
