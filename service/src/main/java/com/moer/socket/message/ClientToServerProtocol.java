package com.moer.socket.message;

import com.moer.socket.MessageProtocol;
import io.netty.buffer.ByteBuf;

public abstract class ClientToServerProtocol extends MessageProtocol {
    @Override
    protected void encodeMessage(ByteBuf buf) {

    }
}
