package com.moer.socket;

import com.moer.socket.message.ClientToServerProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

public class MessageHandler extends SimpleChannelInboundHandler<ClientToServerProtocol> {
    /**
     * 心跳检车的处理
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        IdleStateEvent  event = (IdleStateEvent)evt;
        switch (event.state()){
            case READER_IDLE://120s没有从客户端收到消息  认为连接已经断开  需要清理服务端相关数据
                SocketApplicationContext.logout();
                break;
        }

    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ClientToServerProtocol messageProtocol) throws Exception {

    }
}
