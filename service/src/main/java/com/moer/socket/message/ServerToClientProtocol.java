package com.moer.socket.message;

import com.moer.socket.MessageProtocol;
import com.moer.socket.config.MessageDirection;
import io.netty.buffer.ByteBuf;

public abstract class ServerToClientProtocol extends MessageProtocol {
    public ServerToClientProtocol(){
        this.messageDirection = MessageDirection.SERVER_TO_CLIENT;
        this.magicNum = MAGIC_NUM;
    }
    @Override
    protected void decodeMessage(ByteBuf buf) {

    }
}
