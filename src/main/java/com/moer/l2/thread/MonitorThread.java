package com.moer.l2.thread;

import com.moer.common.Constant;
import com.moer.common.TraceLogger;
import com.moer.l2.L2ApplicationContext;

/**
 * Created by gaoxuejian on 2019/2/22.
 */
public class MonitorThread extends Thread {
    @Override
    public void run() {
        Thread.currentThread().setName("MonitorThread");
        while (true){
            try {
                TimerThread timerThread = L2ApplicationContext.getInstance().timerThread;
                SubscribeThread subscribeThread = L2ApplicationContext.getInstance().subscribeThread;
                DataSyncToRedisThread dataSyncToRedisThread = L2ApplicationContext.getInstance().dataSyncToRedisThread;
                checkThreadState(timerThread);
                checkThreadState(subscribeThread);
                checkThreadState(dataSyncToRedisThread);
                checkThreadState(this);
                //查看TimerThread中的当前任务数量
                int timerTaskSize = timerThread.taskLisk.size();
                TraceLogger.trace(Constant.MONITOR_TRACE,"timerThread 任务数量：" + timerTaskSize);
                TraceLogger.trace(Constant.MONITOR_TRACE,"DispatchServer 执行信息：" + DispatchServer.getState());
                Thread.sleep(5000);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    private String getState(State state){
        if (State.NEW.equals(state)){
            return "new";
        }else if(State.BLOCKED.equals(state)){
            return "blocked";
        }else if(State.RUNNABLE.equals(state)){
            return "runnable";
        }else if(State.TERMINATED.equals(state)){
            return "terminated";
        }else if(State.TIMED_WAITING.equals(state)){
            return "timed_waiting";
        }else if(State.WAITING.equals(state)){
            return "waiting";
        }
        return "";
    }

    private void checkThreadState(Thread thread){
        State state = thread.getState();
        String name = thread.getName();
        String format = "ThreadName:%s, ThreadState:%s！";
        String threadInfo = String.format(format,name,state);
        if (state.equals(State.TERMINATED)){
            thread.start();
            threadInfo = threadInfo + "Restart thread because it is terminated";
        }
        TraceLogger.trace(Constant.MONITOR_TRACE,threadInfo);
    }
}
