package com.moer.zookeeper;

/**
 * Created by gaoxuejian on 2018/5/14.
 */
public class ZkConfig
{
    private String host;
    private int port;
    private String mode;

    public ZkConfig(){};

    public ZkConfig(ZkConfig zkConfig){
        host = zkConfig.getHost();
        port = zkConfig.getPort();
        mode = zkConfig.getMode();
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

    public String getMode() {
        return mode;
    }

    public void setMode(String model) {
        this.mode = model;
    }
}
