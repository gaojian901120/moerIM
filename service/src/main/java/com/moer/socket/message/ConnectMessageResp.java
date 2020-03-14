package com.moer.socket.message;

import com.moer.socket.MessageProtocol;
import com.moer.socket.config.MessageDirection;
import com.moer.socket.config.MessageType;
import com.moer.socket.config.MessageVersion;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;

public class ConnectMessageResp extends ServerToClientProtocol {
    private byte code;
    private String channelId;
    public ConnectMessageResp(byte code, String channelId){
        super();
        this.messageVersion = MessageVersion.V1;
        this.messageType = MessageType.CONNECT;
        this.messageLength =1+channelId.getBytes().length;
        this.code =  code;
        this.channelId = channelId;
    }

    @Override
    public void encodeMessage(ByteBuf buf) {
        buf.writeByte(code);
        byte[] channel = channelId.getBytes();
        buf.writeBytes(channel);
    }
}
