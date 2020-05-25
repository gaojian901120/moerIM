package com.moer.socket.message;

import com.moer.socket.AsyncThreadPool;
import com.moer.socket.MessageProtocol;
import com.moer.socket.SocketApplicationContext;
import com.moer.socket.SocketContext;
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

    //接收到链接消息后 将任务投递到异步线程池中执行
    @Override
    protected boolean handle() {
        AsyncThreadPool.submit(()->{
            //首先根据uid判断当前用户是否已经连接到服务端
            SocketContext context = SocketApplicationContext.getContext(uid);
            if(context == null){

            }
        });
        return false;
    }
}
