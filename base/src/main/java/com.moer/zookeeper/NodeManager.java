package com.moer.zookeeper;

import com.moer.common.Constant;
import com.moer.config.NettyConfig;
import com.moer.util.CryptUtil;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.IntStream;

import static com.moer.common.Constant.ZK_IM_ROOT_NODE_NAME;

/**
 * Created by gaoxuejian on 2018/5/11.
 * 维护和zk之间的连接，同步管理其他节点的状态
 */
public class NodeManager implements Watcher,Runnable {
    public static final Logger logger = LoggerFactory.getLogger(NodeManager.class);
    //每个物理节点对应的虚拟节点的数目，这里假设每台机器的配置一样 所以设置为20 实际上应该根据服务器的配置进行调整。每个物理节点对应的虚拟节点数量越多，平衡性越好，但是性能会稍微有些下降
    private static int VITRUAL_NODE_NUM = 200;
    private static String VITRUAL_SEPARTOR = "##";
    public ServerNode l1ServerNode;
    public volatile SortedMap<String, ServerNode> realNodeList;
    public volatile SortedMap<Integer, String> vitrualNode;
    private NettyConfig nettyConfig;
    private ZkConfig zkConfig;
    private ZooKeeper zk;
    private boolean dead = true;
    private String node = ""; // l1 or  l2
    private String rootPath;
    private static class NodeManagerHolder {
        private static final NodeManager instance = new NodeManager();
    }

    private NodeManager() {
        l1ServerNode = new ServerNode();

    }

    public final static NodeManager getInstance() {
        return NodeManagerHolder.instance;
    }
    public void setConfig(ZkConfig zConfig, NettyConfig nConfig, String node){
        zkConfig = new ZkConfig(zConfig);
        nettyConfig = new NettyConfig(nConfig);
        rootPath = Constant.ZK_IM_ROOT_NODE_NAME + "/" + zkConfig.getMode();
        this.node = node;
    }
    @Override
    public void run() {
        while (true) {
            synchronized (this) {
                try {
                    Thread.sleep(100);
                    if (dead) {
                        if (node.equals("l1")) buildL1(node);
                        else buildL2(node);
                    } else {
                        wait();
                    }
                } catch (Exception e) {
                    logger.error("Zookeeper exception:" + e.getMessage());
                    dead = true;
                }
            }
        }
    }

    public boolean init() {
        try {
            realNodeList = new TreeMap<>();
            vitrualNode = new TreeMap<>();
            zk = new ZooKeeper(zkConfig.getHost(), 3000, this);
            return true;
        } catch (Exception e) {
            logger.error("init zookeeper server failed with config {}", zkConfig.toString());
            return false;
        }
    }

    public boolean buildL2(String type){
        dead = true;
        if (!init())
            return false;
        if (!createRootNode())
            return false;
        if (!createNode("child", nettyConfig.getHostName(), String.valueOf(nettyConfig.getPort())))
            return false;
        if (!checkAndMonitorChildStat())
            return false;
        dead = false;
        node = "l2";
        return true;
    }
    public boolean buildL1(String type){
        dead = true;
        if (!init())
            return false;
        if (!createRootNode())
            return false;
        if (!createNode("master", nettyConfig.getHostName(), String.valueOf(nettyConfig.getPort())))
            return false;
        if (!checkAndMonitorChildStat())
            return false;
        dead = false;
        node = "l1";
        return true;
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

    public boolean createNode(String type, String name, String port) {
        String masterPrefix = ZK_IM_ROOT_NODE_NAME + "/" +zkConfig.getMode() + "/master:";
        String childPrefix = ZK_IM_ROOT_NODE_NAME + "/" +zkConfig.getMode() + "/child:";
        try {
            //创建该节点的子节点
            if (type.equals("master")) {
                String path = String.format("%s%s:%s",masterPrefix, name, port);
                Stat stat = zk.exists(path,false);
                if (stat == null) {
                    zk.create(path, ServerNode.L1_ACCEPT.getBytes("UTF-8"), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                }
                return true;
            } else if (type.equals("child")) {
                String path = String.format("%s%s:%s",childPrefix, name, port);
                Stat stat = zk.exists(path,false);
                if (stat == null) {
                    zk.create(path, ServerNode.L2_ACCEPT.getBytes("UTF-8"), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error(String.format("init  node: %s, port: %s failed with excetion: %s", name, port, e.getMessage()));
            return false;
        }
    }

    public boolean createRootNode() {
        try {
            Stat stat = zk.exists(rootPath, false);//创建删除节点 设置数据 触发监控器
            if (stat == null) {
                //节点不存在 创建节点
                zk.create(rootPath, ServerNode.L1_ACCEPT.getBytes("UTF-8"), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }else {
                zk.sync(rootPath,null,null);
            }
            return true;
        } catch (Exception e) {
            logger.error(String.format("init root /moer_im failed with exception: %s", e.getMessage()));
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
            List<String> children = zk.getChildren(rootPath, this);//watcher 删除本节点或者创建删除子节点触发  getData 删除节点 设置数据
            if (children != null && children.size() > 0) {
                for (String childPath : children) {
                    syncAddZkInfo(childPath);
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

    public void syncAddZkInfo(String path) {
        String[] pathArr = path.split(":");
        //master节点
        if (path.contains("master")) {
            l1ServerNode.setPort(Integer.valueOf(pathArr[2]));
            l1ServerNode.setHost(pathArr[1]);
            l1ServerNode.setName(l1ServerNode.getName() + ":" + l1ServerNode.getPort());
        } else if (path.contains("child")) {
            //child节点
            addServerNode(new ServerNode(pathArr[1], Integer.valueOf(pathArr[2]), path));
        }
        try {
            Stat stat = zk.exists(rootPath + "/" + path, this);
        } catch (Exception e) {

        }

    }

    public void addServerNode(ServerNode node) {
        realNodeList.putIfAbsent(node.getName(), node);
        IntStream.range(0, VITRUAL_NODE_NUM)
                .forEach(index -> {
                    int hash = getHash(String.valueOf(node.getName() + VITRUAL_SEPARTOR + index));
                    vitrualNode.put(hash, node.getName());
                });
        System.out.println(nettyConfig.getPort() + "当前服务节点：");
        for (Map.Entry<String, ServerNode> sn : realNodeList.entrySet()) {
            System.out.println(sn.getValue().getName());
        }
    }

    public void deleteServerNode(ServerNode node) {
        realNodeList.remove(node.getName());
        IntStream.range(0, VITRUAL_NODE_NUM)
                .forEach(index -> {
                    int hash = getHash(String.valueOf(node.getName() + VITRUAL_SEPARTOR + index));
                    vitrualNode.remove(hash);
                });
        System.out.println(nettyConfig.getPort() + "当前服务节点：");
        for (Map.Entry<String, ServerNode> sn : realNodeList.entrySet()) {
            System.out.println(sn.getValue().getName());
        }
    }

    public ServerNode getServerNode(int uid) {
        int hash = getHash(String.valueOf(uid));
        SortedMap<Integer, String> subMap = vitrualNode.tailMap(hash);
        Integer i = null;
        if (subMap.isEmpty()) {
            if (vitrualNode.size() == 0) return null;
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

    //@TODO  添加自定义关闭的操作
    public void process(WatchedEvent watchedEvent) {
        System.out.println("++++++++++++++++++++++++++++" + watchedEvent.toString());
        try {
            String path = watchedEvent.getPath();
            Event.KeeperState keepState = watchedEvent.getState();
            Event.EventType eventType = watchedEvent.getType();
            if (path != null) {
                String[] pathArr = path.split(":");
                if (path.length() > rootPath.length()) { //子节点
                    if (eventType == Event.EventType.NodeCreated) {
                        System.out.println("-----------------NodeCreated---" + path);
                        syncAddZkInfo(path);
                    } else if (eventType == Event.EventType.NodeDeleted) {
                        if (path.contains("child")) {
                            System.out.println("-----------------NodeDeleted---" + path);
                            deleteServerNode(new ServerNode(pathArr[1], Integer.valueOf(pathArr[2]), path.substring(rootPath.length() + 1)));
                            // 如果删除的是自己 说明之前重启后的消息同步过来 这个情况 需要重新添加一遍
                        }
                        if(pathArr[1].equals(nettyConfig.getHostName()) && pathArr[2].equals(String.valueOf(nettyConfig.getPort()))){
                            if (path.contains("child")) {
                                buildL2(node);
                            }else {
                                buildL1(node);
                            }
                        }
                    }
                } else if (path.equals(rootPath)) {//只方便处理新增子节点的情况
                    System.out.println("-----------------Root Node Changed---" + path);

                    checkAndMonitorChildStat();
                }
            }
            System.out.println("sessionId: " + zk.getSessionId() + "  keepState: " + keepState);
            if (eventType == Event.EventType.None) {
                switch (keepState) {
                    case  SyncConnected:
                        break;
                    case Disconnected:
                    case Expired:
                        synchronized (this) {
                            dead = true;
                            notifyAll();
                        }
                        break;
                    }
            }
        } catch (Exception e) {
        }

    }

    public ZooKeeper getZk() {
        return zk;
    }

    /**
     * 获取所有服务节点的hash值
     * 排序好 保证同样的节点 顺序不同 hash值不会有影响
     * @return
     */
    public String getNodeHash() {
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, ServerNode> sn : realNodeList.entrySet()) {
            sb.append(sn.getValue().getName());
        }
        return CryptUtil.md5(sb.toString());
    }

}
