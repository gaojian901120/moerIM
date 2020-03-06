package com.moer.common;

import com.alibaba.fastjson.JSON;
import com.moer.entity.ImSession;
import com.moer.redis.RedisStore;
import com.moer.util.CryptUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaoxuejian on 2018/6/5.
 */
public class ActionHandler {
    public static String renderResult(int code, Object data) {
        return  renderResult(code, com.moer.common.Constant.codeMap.get(code), data);
    }

    public static String renderResult(int code, String message , Object data) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("message", message);
        map.put("data", data);
        String result =  JSON.toJSONString(map);
        map.clear();
        return result;
    }
    public static String getLoginUid(String pptId) {
        try {
            pptId = pptId.replace("-","+").replace("_","/");
            String decode = CryptUtil.authcode(pptId,"293nAs9u23l&29", CryptUtil.DiscuzAuthcodeMode.Decode,0);
            if(decode.contains("_")){
                String [] decodeArr =  decode.split("_");
                return decodeArr[0];
            }
            return "";
        }catch (Exception e){
            return "";
        }
    }
    public static void main(String [] args){
        try {
            String pptId = "7d659523f3184ece8954b927ae5c8704";
            pptId = pptId.replace("-","+").replace("_","/");
            String decode = CryptUtil.authcode(pptId,"293nAs9u23l&29", CryptUtil.DiscuzAuthcodeMode.Decode,0);
            if(decode.contains("_")){
                String [] decodeArr =  decode.split("_");
                System.out.println(decodeArr);
            }
            System.out.println("");
        }catch (Exception e){
            System.out.println(e);
        }
    }
    public static boolean isLogin(HttpHeaders headers,  Map<String, String> params){
        String uid = "";
        String clientToken = "";
        String from = params.get("from");
        RedisStore sessionRedis = ServiceFactory.getSessionRedis();
        String serviceToken = "";
        if(ImSession.FROM_WEB.equals(from)){
            uid = params.get("uid");
            clientToken = params.get("_xx_ppt_token");
            serviceToken = sessionRedis.get("userPptId"+uid);
        }else if(ImSession.FROM_ANDROID.equals(from) || ImSession.FROM_IOS.equals(from)) {
            uid = headers.get("uid");
            clientToken = headers.get("token");
            serviceToken = sessionRedis.get("apptoken" + uid);
        }

        if (clientToken != null && clientToken.length() > 0 && clientToken.equals(serviceToken)){
            return true;
        }
        return false;
    }
    public static String getLoginUid(HttpHeaders headers, Map<String, String> params){
        String uid = "";
        String from = params.get("from");
        if(ImSession.FROM_WEB.equals(from)){
            uid = params.get("uid");
        }else if(ImSession.FROM_ANDROID.equals(from) || ImSession.FROM_IOS.equals(from)) {
            uid = headers.get("uid");
        }
        return uid;
    }
    public static void sendHttpResp(Channel channel, String response, boolean sync){
        try {
            byte[] responseB = new byte[0];
            try {
                responseB = response.getBytes("UTF-8");
            } catch (Exception e) {
                return;
            }
            ByteBuf resp  = Unpooled.wrappedBuffer(responseB);
            FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, resp);
            httpResponse.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
            httpResponse.headers().set(HttpHeaders.Names.CONTENT_LENGTH, httpResponse.content().readableBytes());
            httpResponse.headers().set(HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            httpResponse.headers().set(HttpHeaders.Names.ACCESS_CONTROL_ALLOW_CREDENTIALS,"true");
            httpResponse.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
            if(sync){
                channel.writeAndFlush(httpResponse).sync().addListener(ChannelFutureListener.CLOSE);
            }else {
                if (channel.isWritable()) {
                    channel.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
                } else {
                    channel.writeAndFlush(httpResponse).sync().addListener(ChannelFutureListener.CLOSE);
                }
            }
            response = null;
        }catch (Exception e){}finally {
            channel.close();
        }
    }
}
