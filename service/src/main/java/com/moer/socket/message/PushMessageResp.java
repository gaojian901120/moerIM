package com.moer.socket.message;

import io.netty.buffer.ByteBuf;

public class PushMessageResp extends ClientToServerProtocol{
    private long messageId;

    @Override
    protected void decodeMessage(ByteBuf buf) {
        messageId = buf.readLong();
    }
}
