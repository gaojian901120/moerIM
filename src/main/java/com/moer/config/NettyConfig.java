package com.moer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by gaoxuejian on 2018/5/1.
 */
@Configuration
@ConfigurationProperties(value = "netty")
@PropertySource(value = "classpath:netty.properties")
public class NettyConfig {
    private int port;

    private boolean useEpoll;

    private boolean useSsl;

    private String hostName;

    private int maxHttpContnetLength;

    private boolean useHttpCompress;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isUseEpoll() {
        return useEpoll;
    }

    public void setUseEpoll(boolean useEpoll) {
        this.useEpoll = useEpoll;
    }

    public boolean isUseSsl() {
        return useSsl;
    }

    public void setUseSsl(boolean useSsl) {
        this.useSsl = useSsl;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getMaxHttpContnetLength() {
        return maxHttpContnetLength;
    }

    public void setMaxHttpContnetLength(int maxHttpContnetLength) {
        this.maxHttpContnetLength = maxHttpContnetLength;
    }

    public boolean isUseHttpCompress() {
        return useHttpCompress;
    }

    public void setUseHttpCompress(boolean useHttpCompress) {
        this.useHttpCompress = useHttpCompress;
    }
}
