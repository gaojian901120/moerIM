package com.moer;

import com.moer.server.PushMessageServer;
import com.moer.util.ConfigUtil;
import com.moer.zookeeper.NodeManager;
import com.moer.zookeeper.ServerNode;
import com.moer.zookeeper.ZkConfig;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by gaoxuejian on 2018/5/15.
 * 入口服务:用户首次进入连接该服务器获取提供服务的节点地址
 * 程序启动顺序
 * 1、启动服务层节点，节点启动后，将节点注册到zookeeper，
 * 2、服务节点全部启动完毕后，启动连接层节点，连接层节点获取zookeeper已经注册的服务层节点信息，生成转发规则，同时告诉服务层节点可以初始化数据了。
 * 3、服务层节点收到初始化数据的信息后，进行数据初始化并打开服务端口，开始提供服务。然后告诉连接层节点 可以对外提供服务了。
 * 4、连接层节点收到服务已经启动的信息后，打开端口，正式对外提供服务。
 * 以上不同节点之间的交互都是通过zookeeper进行交互的。
 * 连接层节点的状态：1、imregiste（服务节点注册到zk完成），3、imdatainit(业务数据初始化完成)
 * 服务层节点的状态：2、entrtyconnect(入口节点注册到zk完成) 4、work（正式提供服务）
 * 当新增节点的时候所有 新节点一次进行到1，2状态后
 *
 */
public class ImStatusApplication
{
    public static final Logger logger = LoggerFactory.getLogger(ImStatusApplication.class);
    public static void main(String[] args)
    {
        //连接zookeeper
        ZkConfig zkConfig = ConfigUtil.loadZkConfig();
        try {
            NodeManager nodeManager = new NodeManager(zkConfig);
            ZooKeeper zooKeeper = nodeManager.getZk();
            Stat stat = zooKeeper.exists(Constant.ZK_IM_ROOT_NODE_NAME,false);//创建删除节点 设置数据 触发监控器
            if (stat == null) {
               //节点不存在 创建节点
                zooKeeper.create(Constant.ZK_IM_ROOT_NODE_NAME, String.valueOf(System.currentTimeMillis()).getBytes("UTF-8"), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            List<String> children = zooKeeper.getChildren(Constant.ZK_IM_ROOT_NODE_NAME, nodeManager);//watcher 删除本节点或者创建删除子节点触发  getData 删除节点 设置数据
            if(children != null && children.size() > 0) {
                for (String childPath :children) {
                    zooKeeper.getData();
                }
            }


        }catch (Exception e){
            logger.error("zookeeper start error, message: " + e.getMessage());
        }
    }

    public volatile static List<ServerNode> realNodeList = new LinkedList<>();
    public volatile static SortedMap<Integer,String> vitrualNode = new TreeMap<>();
    //每个物理节点对应的虚拟节点的数目，这里假设每台机器的配置一样 所以设置为20 实际上应该根据服务器的配置进行调整。每个物理节点对应的虚拟节点数量越多，平衡性越好，但是性能会稍微有些下降
    private static int VITRUAL_NODE_NUM = 20;
    private static String VITRUAL_SEPARTOR = "##";

    /**
     * FNV1_32_HASH hash算法
     * @param str
     * @return
     */
    public static int getHash(String str)
    {
        final int p = 16777619;
        int hash = (int)2166136261L;
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
}
