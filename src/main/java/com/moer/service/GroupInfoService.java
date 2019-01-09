package com.moer.service;

import com.moer.bean.GroupInfo;
import com.moer.common.Constant;
import com.moer.common.MapperFactory;
import com.moer.common.ServiceFactory;
import com.moer.dao.mysql.GroupInfoMapper;
import com.moer.redis.RedisStore;

/**
 * Created by gaoxuejian on 2018/6/20.
 */
public class GroupInfoService extends BaseService
{
    public GroupInfo getByGid(String gid)
    {
        GroupInfoMapper groupInfoMapper = MapperFactory.createMapper(GroupInfoMapper.class, DATA_SOURCE_LIVE);
        return groupInfoMapper.selectByGid(gid);
    }

    /**
     * 设置直播间在线人数
     * @param gid
     */
    public boolean setOnlineNum(String gid, int num)
    {
        RedisStore redisStore = ServiceFactory.getRedis();
        return redisStore.hset(Constant.REDIS_GROUP_STATUS + gid, Constant.REDIS_GROUP_STATUS_FIELD_ONLINENUM, String.valueOf(num));
    }

    /**
     * 将直播间在线人数在当前基础上增减
     * @param gid
     * @param num
     * @return
     */
    public Long incrOnlineNum(String gid, int num){
        RedisStore redisStore = ServiceFactory.getRedis();
        return redisStore.hincr(Constant.REDIS_GROUP_STATUS + gid,Constant.REDIS_GROUP_STATUS_FIELD_ONLINENUM, num);
    }
}
