package com.moer.service;

import com.moer.bean.GroupInfo;
import com.moer.common.Constant;
import com.moer.common.MapperFactory;
import com.moer.common.ServiceFactory;
import com.moer.config.NettyConfig;
import com.moer.dao.mysql.GroupInfoMapper;
import com.moer.redis.RedisStore;
import com.moer.util.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by gaoxuejian on 2018/6/20.
 */
public class GroupInfoService extends BaseService
{
    private static Logger logger = LoggerFactory.getLogger(GroupInfoService.class);
    public GroupInfo getByGid(String gid)
    {
        try {
            GroupInfoMapper groupInfoMapper = MapperFactory.createMapper(GroupInfoMapper.class, DATA_SOURCE_LIVE);
            return groupInfoMapper.selectByGid(gid);
        }catch (Exception e){
            logger.error(gid + "get group info exception: ",e);
            return null;
        }

    }


    /**
     * 将直播间在线人数在当前基础上增减
     * @param gid
     * @param num
     * @return
     */
    public Long incrOnlineNum(String gid, int num){
        try{
            RedisStore redisStore = ServiceFactory.getRedis();
            if(redisStore == null) return 0L;
            NettyConfig config  = ConfigUtil.loadNettyConfig();
            String field = config.getHostName() + ":" + config.getPort();
            return redisStore.hincr(Constant.REDIS_GROUP_STATUS_ONLINENUM + gid,field, num);
        }catch (Exception e){
            return 0L;
        }

    }
}
