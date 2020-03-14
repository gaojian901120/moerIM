package com.moer.socket;

import com.moer.socket.config.MessageOffset;
import com.moer.socket.config.MessageType;
import com.moer.socket.config.MessageVersion;
import com.moer.socket.message.ClientToServerProtocol;
import com.moer.socket.message.ConnectMessage;
import com.moer.socket.message.PingMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MessageDecoder extends ByteToMessageDecoder {
    private static final Logger logger = LoggerFactory.getLogger(MessageDecoder.class);
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        String channelId = channelHandlerContext.channel().id().asShortText();
        logger.debug("channel {} received bytes:\n {}",channelId, ByteBufUtil.prettyHexDump(byteBuf));
        //解析协议未具体的协议对象

    }
    private ClientToServerProtocol getMessage(ByteBuf buf) throws Exception{
        ClientToServerProtocol protocol;
        //先读取消息版本
        byte messageVersion = buf.getByte(MessageOffset.MESSAGE_VERSION);
        if(messageVersion == MessageVersion.V1){
            byte messageType = buf.getByte(MessageOffset.V1.MESSAGE_TYPE);
            if(messageType == MessageType.CONNECT){
                protocol = new ConnectMessage();
            }else if(messageType == MessageType.PING){
                protocol = new PingMessage();
            }else if(messageType == MessageType.PUSH){
                protocol = new
            }
        }
    }
}
