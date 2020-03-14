package com.moer.socket.config;

import java.util.HashMap;
import java.util.Map;

public class MessageCode {
    // success
    public static final byte SUCCESS = 0;
    // 消息解码失败
    public static final byte MESSAGE_DECODE_ERROR = 1;
    private static Map<Byte,String> map = new HashMap<>();
    static {
        map.put(SUCCESS,"success");
        map.put(MESSAGE_DECODE_ERROR,"message decode error");
    }
    public static String getError(int code){
        return map.get(code);
    }
}
