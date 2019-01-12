package com.moer.common;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.HashMap;
import java.util.Map;

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
    public static final String ZK_IM_CHIID_NODE_NAME_PREFIX = ZK_IM_ROOT_NODE_NAME + "/child:"; //格式 prefix:ip:port  data 保存当前节点的状态
    public static final String ZK_IM_MASTER_NODE_NAME_PREFIX = ZK_IM_ROOT_NODE_NAME + "/master:"; //格式 prefix:ip:port

    //群组在线人数
    public static final String REDIS_GROUP_STATUS_ONLINENUM = "im:group:status:onlinenum:";// hset  key gid  field  addr+port   value: 节点的数据
    //群组在线人数集合
    public static final String REDIS_GROUP_SET_ONLINEUSER = "im:group:onlineset:";
    //用户状态hset
    public static final String REDIS_USER_STATUS = "im:user:status:";
    public static final String REDIS_USER_STATUS_FIELD_ONLINE = "online"; //用戶在綫狀態
    public static final String USER_ONLINE = "1";
    public static final String USER_OFFLINE = "0";

    //所有在线用户集合
    public static final String REDIS_USER_ONLINE_SET = "im:set:onlineuser";
    public static final int ONLINE_DURATION = 60000;// 60s
    //未读消息数 还是根据序列号来计算

    public static final int CODE_SUCCESS = 1000;
    public static final int CODE_INVALID_REQUEST_METHOD = 1001;
    public static final int CODE_UNLOGIN = 1003;
    public static final int CODE_INVALID_SOURCE = 1002;
    public static final int CODE_NODE_EXPIRED = 1004;
    public static final int CODE_PARAM_ERROR = 1005;
    public static final int CODE_NO_SERVER_NODE = 1006;
    public static final int CODE_UNCONNECT = 1007;
    public static final int CODE_MULTI_END_ERROR = 1008;
    public static Map<Integer, String> codeMap = new HashMap<>();
    static {
        codeMap.put(CODE_SUCCESS,"success");
        codeMap.put(CODE_UNLOGIN, "user not login");
        codeMap.put(CODE_INVALID_REQUEST_METHOD, "invalid request method");
        codeMap.put(CODE_INVALID_SOURCE,"invalid request source");
        codeMap.put(CODE_NODE_EXPIRED,"server info expired, please refresh server list");
        codeMap.put(CODE_PARAM_ERROR, "param error");
        codeMap.put(CODE_NO_SERVER_NODE, "no server to service, maybe the server node is down");
        codeMap.put(CODE_UNCONNECT, "please connect the server node first");
        codeMap.put(CODE_MULTI_END_ERROR, "multiend login");
    }

    public static final Marker MESSAGE_TRACE = MarkerFactory.getMarker("MessageTrace");
    public static final Marker REQUEST_TRACE = MarkerFactory.getMarker("RequestTrace");
}
