package com.moer.redis;


import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import static com.moer.l1.L1EntryApplication.logger;

/**
 * Created by gaoxuejian on 2019/1/11.
 */
public class RedisProxy implements InvocationHandler {
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
