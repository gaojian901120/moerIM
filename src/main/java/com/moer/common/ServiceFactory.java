package com.moer.common;

import com.moer.redis.RedisConfig;
import com.moer.redis.RedisStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaoxuejian on 2018/6/20.
 * 主要管理资源
 */
public class ServiceFactory
{
    private static Logger logger = LoggerFactory.getLogger(ServiceFactory.class);
    private static Map<String,Object> serviceMap = new HashMap<>();
    private static RedisStore redisStore;
    public static boolean init(RedisConfig redisConfig) {
        try {
            redisStore = new RedisStore(redisConfig);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取service层类的实例
     * @param T
     * @param <T>
     * @return
     */
    public static  <T> T getInstace(Class T)
    {
        if (serviceMap.containsKey(T.getName())){
            return (T)serviceMap.get(T.getName());
        }
        synchronized (serviceMap){
            if (serviceMap.containsKey(T.getName())){
                return (T)serviceMap.get(T.getName());
            }
            try {
                T t = (T)T.newInstance();
                serviceMap.put(T.getName(), t);
                return t;
            }catch (Exception e){
                logger.error("get instance {} failed",T.getName());
                return null;
            }
        }
    }

    /**
     * 获取redis实例 单例模式
     * @return
     */
    public static RedisStore getRedis()
    {
        return redisStore;
    }
}
