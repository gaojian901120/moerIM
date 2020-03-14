package com.moer.socket.config;

public class MessageDirection {
    public static final byte SERVER_TO_CLIENT = 0x01;
    public static final byte CLIENT_TO_SERVER = 0x02;
    public static boolean isValid(byte direction){
        if(direction == SERVER_TO_CLIENT){
            return true;
        }
        return false;
    }
}
