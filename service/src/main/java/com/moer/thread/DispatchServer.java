package com.moer.thread;

import com.moer.handler.MessageDispatchHandler;

import java.util.concurrent.*;

/**
 * Created by gaoxuejian on 2018/5/3.
 */
public class DispatchServer {
    /**
     * 优先级队列 透传消息>大V消息>普通群组消息>普通私信>小秘书群发私信
     */
    private static final ExecutorService executor = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>(100000));//CPU核数4-10倍
    public static void dispatchMsg(MessageDispatchHandler task) {
        executor.submit(task);
    }
    public static String getState(){
        ThreadPoolExecutor tpe = ((ThreadPoolExecutor) executor);
        String format = "当前活动任务数：%d; 当前排队任务数：%d; 已完成任务数：%d; 总任务数：%d;";
        return String.format(format,tpe.getActiveCount(),tpe.getQueue().size(),tpe.getCompletedTaskCount(),tpe.getTaskCount());
    }
    public static int getQueued(){
        ThreadPoolExecutor tpe = ((ThreadPoolExecutor) executor);
        return tpe.getQueue().size();
    }
}
