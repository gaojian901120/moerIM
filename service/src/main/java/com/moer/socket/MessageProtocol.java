package com.moer.socket;

import com.moer.socket.config.MessageDirection;
import com.moer.socket.config.MessageType;
import com.moer.socket.config.MessageVersion;
import com.moer.socket.exception.MessageDecodeException;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter(value = AccessLevel.PUBLIC)
@Setter(value = AccessLevel.PUBLIC)
public abstract class MessageProtocol {
    public final static int MAGIC_NUM = 0xa9f3;
    //魔法数  默认0xa9f3
    protected int magicNum;
    //消息版本
    protected byte messageVersion;
    //消息类型
    protected byte messageType;
    //消息方向
    protected byte messageDirection;
    //消息正文长度
    protected int messageLength;
    //CRC16校验
    protected short  messageCRC;
    abstract protected void encodeMessage(ByteBuf buf);
    abstract protected void decodeMessage(ByteBuf buf);
    public void encode(ByteBuf buf){
        int start = buf.readableBytes();
        buf.writeInt(magicNum);
        buf.writeByte(messageVersion);
        buf.writeByte(messageType);
        buf.writeByte(messageDirection);
        buf.writeInt(messageLength);
        encodeMessage(buf);
        int end = buf.readableBytes();
        byte [] crcData = new byte[end-start];
        buf.getBytes(start,crcData);
        messageCRC = (short) CrcUtil.calcCrc16(crcData);
        buf.writeShort(messageCRC);
    }
    public void decode(ByteBuf buf) throws Exception{
        magicNum = buf.readInt();
        messageVersion = buf.readByte();
        messageType = buf.readByte();
        messageDirection = buf.readByte();
        messageLength = buf.readInt();
        decodeMessage(buf);
        messageCRC = buf.readShort();
        if(magicNum != MAGIC_NUM){
            throw new MessageDecodeException("magic num [%d] invalid", magicNum);
        }
        if(!MessageVersion.isValid(messageVersion)){
            throw new MessageDecodeException("message version [%d] invalid", messageVersion);
        }
        if(!MessageType.isValid(messageType)) {
            throw new MessageDecodeException("message type [%d] invalid", messageType);
        }
        if(!MessageDirection.isValid(messageDirection)){
            throw new MessageDecodeException("message direction [%d] invalid" ,messageDirection);
        }
        byte[] crcData = new byte[messageLength+11];
        buf.getBytes(0,crcData);
        short crc = (short) CrcUtil.calcCrc16(crcData);
        if(crc != messageCRC) {
            throw new MessageDecodeException("message crc error,ori[%d],computer[%d] ", messageCRC, crc);
        }
    }
}
