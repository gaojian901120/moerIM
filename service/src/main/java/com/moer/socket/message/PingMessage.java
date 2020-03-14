package com.moer.socket.message;

import io.netty.buffer.ByteBuf;

public class PingMessage extends ClientToServerProtocol{
    @Override
    protected void decodeMessage(ByteBuf buf) {

    }
}
