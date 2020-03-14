package com.moer.socket.config;

public class MessageType {
    public static final byte  CONNECT = 0x01;
    public static final byte  PING = 0x02;
    public static final byte  PUSH = 0x03;

    public static boolean isValid(byte type){
        if(type == CONNECT || type == PING || type == PUSH){
            return true;
        }
        return false;
    }
}
