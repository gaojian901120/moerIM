package com.moer.zookeeper;

/**
 * Created by gaoxuejian on 2018/5/14.
 */
public class ZkConfig {
    private String host;
    private String mode;

    public ZkConfig() {
    }

    ;

    public ZkConfig(ZkConfig zkConfig) {
        host = zkConfig.getHost();
        mode = zkConfig.getMode();
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String model) {
        this.mode = model;
    }

    @Override
    public String toString() {
        return String.format("Host:%s, Mode:%s", host, mode);
    }
}
