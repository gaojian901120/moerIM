package com.moer.socket.message;

import com.moer.socket.MessageProtocol;
import io.netty.buffer.ByteBuf;

public class ConnectMessage extends ClientToServerProtocol {
    private int uid;
    private String token;

    @Override
    protected void decodeMessage(ByteBuf buf) {
        uid= buf.readInt();
        byte [] bytes = new byte[messageLength-4];
        buf.readBytes(bytes);
        token = new String(bytes);
    }
}
