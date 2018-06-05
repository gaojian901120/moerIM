package com.moer.util;

import com.moer.config.ImConfig;
import com.moer.config.NettyConfig;
import com.moer.redis.RedisConfig;
import com.moer.zookeeper.ZkConfig;

import java.util.ResourceBundle;

/**
 * Created by gaoxuejian on 2018/5/16.
 */
public class ConfigUtil {
    public static RedisConfig loadRedisConfig() {
        RedisConfig redisConfig = new RedisConfig();
        ResourceBundle resourceBundle = ResourceBundle.getBundle("redis");
        redisConfig.setHost(resourceBundle.getString("redis.host"));
        redisConfig.setPort(Integer.parseInt(resourceBundle.getString("redis.port")));
        redisConfig.setMinIdle(Integer.parseInt(resourceBundle.getString("redis.minIdle")));
        redisConfig.setMaxTotal(Integer.parseInt(resourceBundle.getString("redis.maxTotal")));
        redisConfig.setMaxIdle(Integer.parseInt(resourceBundle.getString("redis.maxIdle")));
        redisConfig.setMaxWait(Integer.parseInt(resourceBundle.getString("redis.maxWait")));
        redisConfig.setMinEvictableIdleTimeMillis(Integer.parseInt(resourceBundle.getString("redis.minEvictableIdleTimeMillis")));
        redisConfig.setTimeBetweenEvictionRunsMillis(Integer.parseInt(resourceBundle.getString("redis.timeBetweenEvictionRunsMillis")));
        return redisConfig;
    }

    public static NettyConfig loadNettyConfig() {
        NettyConfig nettyConfig = new NettyConfig();
        ResourceBundle resourceBundle = ResourceBundle.getBundle("netty");
        nettyConfig.setHostName(resourceBundle.getString("netty.hostName"));
        nettyConfig.setPort(Integer.parseInt(resourceBundle.getString("netty.port")));
        nettyConfig.setUseEpoll(Boolean.parseBoolean(resourceBundle.getString("netty.useEpoll")));
        nettyConfig.setUseSsl(Boolean.parseBoolean(resourceBundle.getString("netty.useSsl")));
        nettyConfig.setUseHttpCompress(Boolean.parseBoolean(resourceBundle.getString("netty.userHttpCompress")));
        nettyConfig.setMaxHttpContnetLength(Integer.parseInt(resourceBundle.getString("netty.maxHttpContentLength")));
        return nettyConfig;
    }

    public static ZkConfig loadZkConfig() {
        ZkConfig zkConfig = new ZkConfig();
        ResourceBundle resourceBundle = ResourceBundle.getBundle("zookeeper");
        zkConfig.setPort(Integer.parseInt(resourceBundle.getString("zk.port")));
        zkConfig.setHost(resourceBundle.getString("zk.host"));
        zkConfig.setMode(resourceBundle.getString("zk.mode"));
        return zkConfig;
    }

    public static ImConfig loadImConfig() {
        ImConfig imConfig = new ImConfig();
        ResourceBundle resourceBundle = ResourceBundle.getBundle("im");
        imConfig.setMultiAppEnd(Boolean.valueOf(resourceBundle.getString("im.multiAppEnd")));
        imConfig.setMultiWebEnd(Boolean.valueOf(resourceBundle.getString("im.multiWebEnd")));
        imConfig.setMultiEnd(Boolean.valueOf(resourceBundle.getString("im.multiEnd")));
        return imConfig;
    }
}
