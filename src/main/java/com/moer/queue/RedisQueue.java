package com.moer.queue;

import com.moer.store.RedisStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by gaoxuejian on 2018/5/2.
 */
@Component
public class RedisQueue {
    @Autowired
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
