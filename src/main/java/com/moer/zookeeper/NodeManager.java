package com.moer.zookeeper;

import org.apache.zookeeper.*;

/**
 * Created by gaoxuejian on 2018/5/11.
 * 维护和zk之间的连接，同步管理其他节点的状态
 */
public class NodeManager implements Watcher {
    private ZkConfig zkConfig;
    private ZooKeeper zk;

    public NodeManager(ZkConfig config) throws Exception {
        zkConfig = new ZkConfig(config);
        zk = new ZooKeeper(zkConfig.getHost() + ":" + zkConfig.getPort(), 3000, this);
    }


    @Override
    public void process(WatchedEvent watchedEvent) {
        try {
            System.out.println(watchedEvent.toString());
            zk.getChildren("/test",this);
        }catch (Exception e) {}
    }

    public ZooKeeper getZk() {
        return zk;
    }
}
