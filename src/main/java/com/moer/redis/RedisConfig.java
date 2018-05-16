package com.moer.redis;


/**
 * Created by gaoxuejian on 2018/5/2.
 */
public class RedisConfig {
    private String host;
    private int port;
    private int minIdle;
    private int maxIdle;
    private int maxTotal;
    private int maxWait;
    private int timeBetweenEvictionRunsMillis;
    private int minEvictableIdleTimeMillis;

    public RedisConfig(){}

    public RedisConfig(RedisConfig config)
    {
        host = config.getHost();
        port = config.getPort();
        minIdle = config.getMinIdle();
        maxIdle = config.getMaxIdle();
        maxTotal = config.getMaxTotal();
        maxWait = config.getMaxWait();
        timeBetweenEvictionRunsMillis = config.getTimeBetweenEvictionRunsMillis();
        minEvictableIdleTimeMillis = config.getMinEvictableIdleTimeMillis();
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public int getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public int getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(int maxWait) {
        this.maxWait = maxWait;
    }

    public int getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    public void setTimeBetweenEvictionRunsMillis(int timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    public int getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    public void setMinEvictableIdleTimeMillis(int minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }
}
