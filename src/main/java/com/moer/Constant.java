package com.moer;

/**
 * Created by gaoxuejian on 2018/5/3.
 */
public class Constant {
    //订阅的接受消息的频道
    public static final String MSG_RECV_QUEUE = "im_root_queue";
    //数据同步的频道  在启动节点的时候 将所有相关数据加载进来
    public static final String DATA_SYNC_QUEUE = "im_data_sync_queue";
    //zookeeper Im根节点的path
    public static final String ZK_IM_ROOT_NODE_NAME = "/moer_im";
    //根节点下的子节点的path的前缀
    public static final String ZK_IM_CHIID_NODE_NAME_PREFIX = "moer_im_child:"; //格式 prefix:ip:port  data 保存当前节点的状态

}
