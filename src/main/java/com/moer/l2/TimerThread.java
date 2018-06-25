package com.moer.l2;

import com.moer.entity.ImSession;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by gaoxuejian on 2018/6/25.
 */
public class TimerThread extends Thread
{
    public Queue<Object> taskLisk = new LinkedList<>();

    @Override
    public void run() {
        while (true){
            Object o = taskLisk.poll();
            if (o instanceof ImSession){
                ImSession imSession = (ImSession)o;
                long curTime = System.currentTimeMillis();
                long updateTime = imSession.getUpdateTime();
                if (imSession.getChannel().isActive() ||curTime - updateTime >=10 ){//10s内活跃 或者当前连接hold 则用户在线
                    taskLisk.add(o);
                }else  {
                    L2ApplicationContext.getInstance().logout(imSession);
                }
            }
        }
    }
}
