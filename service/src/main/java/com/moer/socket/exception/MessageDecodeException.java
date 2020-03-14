package com.moer.socket.exception;

import com.moer.socket.config.MessageCode;

public class MessageDecodeException extends Exception{
    private int code;
    private String error;
    public MessageDecodeException(String format , Object ... data){
        code = MessageCode.MESSAGE_DECODE_ERROR;
        error = String.format(format,data);
        error = MessageCode.getError(code)+ ":" + error;
    }
}
