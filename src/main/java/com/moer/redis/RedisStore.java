package com.moer.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.List;

/**
 * Created by gaoxuejian on 2018/5/2.
 */
public class RedisStore {
    public static final Logger log = LoggerFactory.getLogger(RedisStore.class);
    private RedisConfig redisConfig;
    private JedisPool jedisPool;

    public RedisStore(RedisConfig pconfig) {
        redisConfig = new RedisConfig(pconfig);
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMinIdle(redisConfig.getMinIdle());
        config.setMaxIdle(redisConfig.getMaxIdle());
        config.setMaxTotal(redisConfig.getMaxTotal());
        config.setMaxWaitMillis(redisConfig.getMaxWait());
        //在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
        config.setTestOnBorrow(true);
        jedisPool = new JedisPool(config, redisConfig.getHost(), redisConfig.getPort());
    }

    /**
     * 连续push多个元素进入队列
     *
     * @param key
     * @param t
     * @return
     */
    public boolean lpush(String key, String... t) {
        if (key == null || t == null || t.length == 0) {
            return false;
        }
        Jedis jedis = null;
        boolean ret = false;

        try {
            jedis = jedisPool.getResource();
            if (jedis != null) {
                Long rst = jedis.lpush(key, t);
                if (rst == 0) {
                    log.error("lpush failed with key:[%s] ,value:[%s]", key, t);
                } else {
                    ret = true;
                }
            }
        } catch (Exception e) {
            log.error("lpush failed with key:[%s] ,value:[%s]", key, t);
        } finally {
            if (null != jedis) {
                jedis.close();
            }
            return ret;

        }
    }

    public String rpop(String key) {
        if (key == null) {
            return null;
        }
        Jedis jedis = null;
        String rst = "";

        try {
            jedis = jedisPool.getResource();
            if (jedis != null) {
                rst = jedis.rpop(key);
                if (rst == null) {
                    log.error("rpop failed with key:[%s] ", key);
                }
            }
        } catch (Exception e) {
            log.error("rpop failed with key:[%s] ", key);
        } finally {
            if (null != jedis) {
                jedis.close();
            }
            return rst;

        }
    }

    public List<String> brpop(int timeout, String... key) {
        if (key == null) {
            return null;
        }
        Jedis jedis = null;
        List<String> rst = null;

        try {
            jedis = jedisPool.getResource();
            if (jedis != null) {
                rst = jedis.brpop(timeout, key);

                if (rst == null) {
                    log.error("brpop failed with key:[%s] ", key);
                }
            }
        } catch (Exception e) {
            log.error("brpop failed with key:[%s] ", key);
        } finally {
            if (null != jedis) {
                jedis.close();
            }
            return rst;

        }
    }

    public void subscribeChannel(String channel, JedisPubSub listener) {
        if (channel == null) {
            return;
        }
        Jedis jedis = null;
        List<String> rst = null;

        try {
            jedis = jedisPool.getResource();
            if (jedis != null) {
                jedis.subscribe(listener, channel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != jedis) {
                jedis.close();
            }
            return;

        }
    }

    public boolean pubishMessage(String channel, String message) {
        if (channel == null) {
            return false;
        }
        Jedis jedis = null;
        boolean rst = false;

        try {
            jedis = jedisPool.getResource();
            if (jedis != null) {
                Long res = jedis.publish(channel, message);
                if (res > 0) rst = true;
            }
        } catch (Exception e) {
        } finally {
            if (null != jedis) {
                jedis.close();
            }
            return rst;

        }
    }
}
