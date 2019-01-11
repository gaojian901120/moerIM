package com.moer.l2;

import com.moer.entity.ImSession;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by gaoxuejian on 2018/6/25.
 */
public class TimerThread extends Thread
{
    public Queue<TimerTask> taskLisk = new LinkedList<>();

    @Override
    public void run() {
        while (true){
            TimerTask task = taskLisk.poll();
            if (task == null) {
                try {
                    Thread.sleep(1000);
                }catch (Exception e){}
                continue;
            }
            long execTime = task.getExecTime();
            long curTime = System.currentTimeMillis();
            if (execTime > curTime) {
                try {
                    Thread.sleep(1000);
                }catch (Exception e){}
            }
            int type = task.getTaskType();
            if (type == TimerTask.TASK_SESSION_CHECK) {
                ImSession imSession = (ImSession)task.getData();
                if (imSession.isVaild()){//30s内活跃 或者当前连接hold 则用户在线
                    task.setExecTime(execTime+10000);
                    taskLisk.add(task);
                }else  {
                    if(imSession.getStatus() != ImSession.SESSION_STATUS_EXPIRED){
                        System.out.println("timerthread用户" + imSession.getUid() + "活跃时间：" + imSession.getUpdateTime());
                        L2ApplicationContext.getInstance().sessionLogout(imSession, "user session expired");
                    }
                }
            }
        }
    }

}
