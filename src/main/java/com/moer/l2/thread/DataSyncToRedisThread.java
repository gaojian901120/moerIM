package com.moer.l2.thread;

import com.moer.bean.GroupMembers;
import com.moer.common.Constant;
import com.moer.common.ServiceFactory;
import com.moer.config.NettyConfig;
import com.moer.entity.ImGroup;
import com.moer.l2.L2ApplicationContext;
import com.moer.redis.RedisStore;
import com.moer.zookeeper.NodeManager;
import com.moer.zookeeper.ServerNode;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Created by gaoxuejian on 2019/1/11.
 */
public class DataSyncToRedisThread extends Thread{
    private static final int SLEEP_TIME = 60000;// 60s
    @Override
    public void run() {
        Thread.currentThread().setName("DataSyncToRedisThread");
        while(true){
            try {
                Map<String,ImGroup> groupMap = L2ApplicationContext.getInstance().IMGroupContext;
                NettyConfig config  = L2ApplicationContext.getInstance().nettyConfig;
                NodeManager nodeManager = NodeManager.getInstance();
                String field = config.getHostName() + ":" + config.getPort();
                RedisStore redis = ServiceFactory.getRedis();
                if(!Objects.isNull(groupMap)){
                    for (Map.Entry<String,ImGroup> groupEntry: groupMap.entrySet()) {
                        Map<Integer,GroupMembers> members = groupEntry.getValue().getUserList();
                        redis.hset(Constant.REDIS_GROUP_STATUS_ONLINENUM + groupEntry.getKey(),field,Objects.isNull(members) ? "0" : members.size()+"");
                    }
                }
                //在线人数集合更新
                long curtime = System.currentTimeMillis();
                Map<String,String> onlineUser = redis.hgetall(Constant.REDIS_USER_ONLINE_SET);
                if(onlineUser != null){
                    for (Map.Entry<String,String> userEntry: onlineUser.entrySet()) {
                        long activetime = Long.valueOf(userEntry.getValue());
                        if(curtime- activetime > Constant.ONLINE_DURATION){
                            redis.hdel(Constant.REDIS_USER_ONLINE_SET, userEntry.getKey());
                        }
                    }
                }
                //更新群组在线人数的集合
                if(!Objects.isNull(groupMap)) {
                    for (Map.Entry<String, ImGroup> groupEntry : groupMap.entrySet()) {
                        Set<String> members =  redis.smembers(Constant.REDIS_GROUP_SET_ONLINEUSER + groupEntry.getKey());
                        if(members != null){
                            Map<Integer,GroupMembers> memberMap =  groupEntry.getValue().getUserList();
                            for (Integer uid: memberMap.keySet()) {
                                redis.sadd(Constant.REDIS_GROUP_SET_ONLINEUSER + groupEntry.getKey(), uid+"");
                            }
                            for (String uid : members) {
                                ServerNode serverNode = nodeManager.getServerNode(Integer.valueOf(uid));
                                boolean thisNode = serverNode.getHost().equals(config.getHostName()) && serverNode.getPort() == config.getPort();
                                if(thisNode){
                                    if(onlineUser == null || !onlineUser.containsKey(uid)){
                                        redis.srem(Constant.REDIS_GROUP_SET_ONLINEUSER + groupEntry.getKey(),uid);
                                    }
                                }
                            }
                        }
                    }
                }
                Thread.sleep(SLEEP_TIME);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
