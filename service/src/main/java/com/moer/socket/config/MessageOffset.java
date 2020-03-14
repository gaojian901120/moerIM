package com.moer.socket.config;

public class MessageOffset {
    public static final int MAGIC_OFFSET = 0;
    public static final int MESSAGE_VERSION = 4;
    public static class V1 {
        public static final int MESSAGE_TYPE = 5;
        public static final int MESSAGE_DIRECTION = 6;
        public static final int MESSAGE_LENGTH = 7;
    }
}
