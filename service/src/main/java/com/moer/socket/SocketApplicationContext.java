package com.moer.socket;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.FutureListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//保存所有长链接的上下文数据
public class SocketApplicationContext {
    //第一层key为Uid， Value为该用户连接的所有的客户端
    private static Map<Integer,ImSocketUser> userMap = new ConcurrentHashMap<>();



    private static Map<Integer, SocketContext> garbageSockets = new ConcurrentHashMap<>();
    /**
     *  分以下集中情况
     *  用户第一次登陆  登陆成功
     *  用户登陆成功后再次通过同一个channel重复发送登陆消息  这种情况 静默处理  默认登陆成功
     *  用户登陆成功后
     */
    public static boolean login(SocketContext context){
        Integer uid = context.getUid();
        ImSocketUser socketUser = userMap.get(uid);
        if(socketUser == null) {
            socketUser = new ImSocketUser(uid);
        }
        Map<String,SocketContext> contextMap = socketUser.getUserSockets();
        SocketContext oldContext = contextMap.get(context.getChannelId());
        //不为空说明 在连接成功后 客户端重复发送了登陆信息，
        if(oldContext != null) {
            if(oldContext.getChannelContext().channel().isActive()){//说明连接状态正常  这种情况下 只更新服务端的心跳时间
                oldContext.setLastHeartBeatTime(System.currentTimeMillis());
            }else{//说明连接已经中断  这种情况应该是刚建立连接 服务端还没有开始处理，客户端已经断开连接  这种情况下 直接关闭 不再做任何处理
                ChannelFuture future = oldContext.getChannelContext().close();
                future.addListener(new ChannelFutureListener(){
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if(future.isSuccess()){
                            System.out.println("client close before server handle login message: " + oldContext.getChannelId());
                        }
                    }
                });
                return false;
            }
        }else {
            contextMap.put(context.getChannelId(),context);//直接插入
        }
        //判断是否有多端登陆
        String deviceToken = context.getDeviceToken();
        for (Map.Entry<String,SocketContext> entry: contextMap.entrySet()) {
            if(entry.getKey()!= context.getChannelId()){//说明不是一个连接

            }
        }
        return true;

    }
    public static void logout(){

    }
}
