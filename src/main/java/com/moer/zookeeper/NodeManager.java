package com.moer.zookeeper;

import com.moer.Constant;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.IntStream;

/**
 * Created by gaoxuejian on 2018/5/11.
 * 维护和zk之间的连接，同步管理其他节点的状态
 */
public class NodeManager implements Watcher {
    public static final Logger logger = LoggerFactory.getLogger(NodeManager.class);
    //每个物理节点对应的虚拟节点的数目，这里假设每台机器的配置一样 所以设置为20 实际上应该根据服务器的配置进行调整。每个物理节点对应的虚拟节点数量越多，平衡性越好，但是性能会稍微有些下降
    private static int VITRUAL_NODE_NUM = 20;
    private static String VITRUAL_SEPARTOR = "##";
    public volatile List<ServerNode> realNodeList = new LinkedList<ServerNode>();
    public volatile SortedMap<Integer, String> vitrualNode = new TreeMap<Integer, String>();

    private ZkConfig zkConfig;
    private ZooKeeper zk;

    public NodeManager(ZkConfig config) throws Exception {
        zkConfig = new ZkConfig(config);
        zk = new ZooKeeper(zkConfig.getHost() + ":" + zkConfig.getPort(), 3000, this);

    }

    /**
     * FNV1_32_HASH hash算法
     *
     * @param str
     * @return
     */
    public static int getHash(String str) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < str.length(); i++)
            hash = (hash ^ str.charAt(i)) * p;
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        // 如果算出来的值为负数则取其绝对值
        if (hash < 0)
            hash = Math.abs(hash);
        return hash;
    }

    public boolean createChildNode(String name, String port) {
        try {
            if (createRootNode()) {
                //创建该节点的子节点
                zk.create(String.format("%s/%s%s:%s", Constant.ZK_IM_ROOT_NODE_NAME, Constant.ZK_IM_CHIID_NODE_NAME_PREFIX, name, port), ServerNode.L2_ACCEPT.getBytes("UTF-8"), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error(String.format("init child node: %s, port: %s failed with excetion: %s", name, port, e.getMessage()));
            return false;
        }
    }

    public boolean createRootNode() {
        try {
            Stat stat = zk.exists(Constant.ZK_IM_ROOT_NODE_NAME, false);//创建删除节点 设置数据 触发监控器
            if (stat == null) {
                //节点不存在 创建节点
                zk.create(Constant.ZK_IM_ROOT_NODE_NAME, ServerNode.L1_ACCEPT.getBytes("UTF-8"), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            return true;
        } catch (Exception e) {
            logger.error(String.format("init node: %s, port: %s failed with excetion: %s", e.getMessage()));
            return false;
        }
    }

    /**
     * 检测子节点状态
     * 初始化L2节点hash环
     * 添加节点状态监控回调方法
     *
     * @return
     */
    public boolean checkAndMonitorChildStat() {
        boolean checkRes = true;
        try {
            List<String> children = zk.getChildren(Constant.ZK_IM_ROOT_NODE_NAME, this);//watcher 删除本节点或者创建删除子节点触发  getData 删除节点 设置数据
            if (children != null && children.size() > 0) {
                for (String childPath : children) {
                    byte[] childDataB = zk.getData(childPath, this, null);
                    String childDataS = new String(childDataB);
                    if (!childDataS.equals(ServerNode.L1_ACCEPT)) {
                        logger.error(String.format("child node %s stat %s not vaild, server start failed", childPath, childDataS));
                        checkRes = false;
                        break;
                    }
                    String[] pathArr = childPath.split(":");
                    addServerNode(new ServerNode(pathArr[1], Integer.valueOf(pathArr[2]), childPath));
                }
            } else {
                logger.error("child node not init, start failed");
                checkRes = false;
            }
        } catch (Exception e) {
            logger.error("checkAndMonitorChildStat failed with excetion: " + e.getMessage());
            checkRes = false;
        }
        return checkRes;
    }

    public void addServerNode(ServerNode node) {
        realNodeList.add(node);
        IntStream.range(0, VITRUAL_NODE_NUM)
                .forEach(index -> {
                    int hash = getHash(String.valueOf(node.getName() + VITRUAL_SEPARTOR + index));
                    vitrualNode.put(hash, node.getName());
                });
    }

    public void deleteServerNode(ServerNode node) {
        realNodeList.removeIf(o -> node.getName().equals(o.getName()));
        IntStream.range(0, VITRUAL_NODE_NUM)
                .forEach(index -> {
                    int hash = getHash(String.valueOf(node.getName() + VITRUAL_SEPARTOR + index));
                    vitrualNode.remove(hash);
                });
    }

    public ServerNode getServerNode(int uid) {
        int hash = getHash(String.valueOf(uid));
        SortedMap<Integer, String> subMap = vitrualNode.tailMap(hash);
        Integer i = null;
        if (subMap.isEmpty()) {
            i = vitrualNode.firstKey();

        } else {
            i = subMap.firstKey();
        }
        String serverName = vitrualNode.get(i);
        String[] serverNameArr = serverName.split(":");
        //返回对应的服务器
        ServerNode sn = new ServerNode(serverNameArr[1], Integer.valueOf(serverNameArr[2]), serverName);
        return sn;
    }

    public void process(WatchedEvent watchedEvent) {
        try {
            System.out.println(watchedEvent.toString());
            zk.getChildren("/test", this);
        } catch (Exception e) {
        }
    }

    public ZooKeeper getZk() {
        return zk;
    }
}
