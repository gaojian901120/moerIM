package com.moer.socket.config;

public class MessageVersion {
    //消息版本号  目前只有一个版本号 为V1
    public static final byte V1 = 0x01;
    public static boolean isValid(byte version){
        if(version == V1){
            return true;
        }
        return false;
    }
}
