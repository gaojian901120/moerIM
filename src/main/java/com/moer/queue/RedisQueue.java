package com.moer.queue;

import com.moer.redis.RedisStore;

import java.util.List;

/**
 * Created by gaoxuejian on 2018/5/2.
 */
public class RedisQueue {
    RedisStore redisStore;

    public boolean push(String key, String s) {
        return redisStore.lpush(key, s);
    }

    public List<String> pop(int timeout, String... key) {
        List<String> ele = redisStore.brpop(timeout, key);
        return ele;
    }

    public int len(String key) {
        return 0;
    }
}
