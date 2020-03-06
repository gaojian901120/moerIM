package com.moer.thread;

import com.alibaba.fastjson.JSON;
import com.moer.common.ActionHandler;
import com.moer.common.Constant;
import com.moer.common.TraceLogger;
import com.moer.entity.ImMessage;
import com.moer.entity.ImSession;
import com.moer.L2ApplicationContext;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * Created by gaoxuejian on 2019/3/7.
 */

public class PushThreadPool {
    private static Logger logger = LoggerFactory.getLogger(PushThreadPool.class);

    private PushThread [] pushThreads = new PushThread[16];
    public void start(){
        for(int i=0;i<16;i++){
            pushThreads[i] = new PushThread(i);
            pushThreads[i].setDaemon(true);
            pushThreads[i].start();
        }
    }
    public void addPushTask(ImSession imSession){
        int uid = imSession.getUid();
        int hashcode = uid % 16;
        pushThreads[hashcode].addTask(imSession);
    }

    public PushThread[] getPushThreads() {
        return pushThreads;
    }

    public Map<Integer,Integer> getPushStatus(){
        Map<Integer,Integer> res = new HashMap<>();
        for (PushThread pt : pushThreads){
            res.put(pt.getHashcode(),pt.getQueued());
        }
        return res;
    }
    class PushThread extends Thread{
        private int hashcode;

        public int getHashcode() {
            return hashcode;
        }

        public PushThread(int hashcode) {
            this.hashcode = hashcode;
            setName("PushThread-"+hashcode);

        }
        public int getQueued(){
            return imSessions.size();
        }
        private LinkedBlockingDeque<ImSession> imSessions = new LinkedBlockingDeque<>();
        @Override
        public void run() {
            while (true){
                try {
                    ImSession session = imSessions.pollFirst(10, TimeUnit.SECONDS);
                    if (session == null) {
                        continue;
                    }
                    int uid = session.getUid();
                    Channel channel = session.getChannel();
                    if (session.isVaild() && channel.isWritable()) {
                        if (session.getStatus() == ImSession.SESSION_STATUS_PULLING) {
                            long updateTime = session.getUpdateTime();
                            long curTime = System.currentTimeMillis();
                            if(curTime-updateTime<=50000){
                                Vector<ImMessage> imMessages = session.popMsgFromTail();
                                if (imMessages.size() == 0) {
                                    continue;
                                }
                                synchronized (session.getMsgLock()) {
                                    if (session.getStatus() == ImSession.SESSION_STATUS_PULLING) {
                                        //channel活跃只表示socket有效 可能多个请求使用同一个channel

                                        StringBuffer midSb = new StringBuffer();
                                        imMessages.forEach(item -> {
                                            midSb.append(item.getMid());
                                            midSb.append(",");
                                        });
                                        Collections.sort(imMessages);

                                        Map<String, Object> data = new HashMap<>();
                                        data.put("code", Constant.CODE_SUCCESS);
                                        data.put("message", "push message success");
                                        data.put("data", L2ApplicationContext.getInstance().convertMessage(imMessages));
                                        String response = JSON.toJSONString(data);
                                        data.clear();
                                        if (session.getSource().equals(ImSession.SESSION_SOURCE_WEB)) {
                                            response = "pullCallback(" + response + ")";
                                        }
                                        if (channel.isWritable()) {
                                            ActionHandler.sendHttpResp(channel, response, true);
                                            imMessages.clear();
                                            session.setStatus(ImSession.SESSION_STATUS_UNPULL);
                                            TraceLogger.trace(Constant.MESSAGE_TRACE, "push message {} to user {} with sessionId {} and channelId {} async", midSb.toString(), uid, session.getSeeesionId(), channel.id().asShortText());
                                        } else {
                                            session.addMsgBlockToHead(imMessages);
                                        }
                                    }
                                }
                            }else {
                                session.getChannel().pipeline().close();
                            }
                         }
                    }
                }catch (Exception e){
                    logger.error("push message exception: ",e);
                }
            }
        }
        public void addTask(ImSession imSession){
            try {
                imSessions.putLast(imSession);
            }catch (Exception e){}
        }
    }
}
