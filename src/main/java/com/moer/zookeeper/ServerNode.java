package com.moer.zookeeper;

/**
 * Created by gaoxuejian on 2018/5/16.
 */
public class ServerNode implements Comparable<ServerNode> {
    //服务层节点和连接层节点得状态 L1开头表是分发层节点状态  L2开头表是服务层节点的状态
    //不初始化数据 根据用户需要第一次请求的时候初始化
    public static final String L1_ACCEPT = "l1_accept"; //l1节点可以开始转发请求了  l1_zk_reg->l1_accept(需要所有L2的节点都处于l2_accept状态)
    public static final String L2_ACCEPT = "l2_accept"; //服务层节点可以开始接受请求了 l2_zk_reg->l2_accept(需要L1节点处于l1_zk_reg,l1_accept)
    private String host;

    private int port;
    private String name;

    public ServerNode() {
    }

    ;
    public ServerNode(String host, int port, String name) {
        this.host = host;
        this.port = port;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(ServerNode o) {
        return this.name.compareTo(o.getName());
    }

}
