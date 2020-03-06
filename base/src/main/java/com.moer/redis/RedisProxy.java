package com.moer.redis;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;


/**
 * Created by gaoxuejian on 2019/1/11.
 */
public class RedisProxy implements InvocationHandler {
    private static final Logger logger = LoggerFactory.getLogger(RedisProxy.class);
    private ShardedJedisPool shardedJedisPool;
    private String poolName;
    public RedisProxy(ShardedJedisPool shardedJedisPool, String poolName){
        this.shardedJedisPool = shardedJedisPool;
        this.poolName = poolName;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        ShardedJedis jedis = shardedJedisPool.getResource();
        Object object = null;
        try {
            object = method.invoke(jedis, args);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        } finally {
            if(jedis != null)
                jedis.close();
        }
        return object;
    }
}
