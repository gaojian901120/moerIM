package com.moer.l2;

/**
 * Created by gaoxuejian on 2018/6/26.
 */
public class TimerTask {
    public static final int TASK_SESSION_CHECK = 1;
    private long execTime;
    private int taskType;
    private Object data;

    public TimerTask(long time, int type, Object data)
    {
        this.execTime = time;
        this.taskType = type;
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public long getExecTime() {
        return execTime;
    }

    public int getTaskType() {
        return taskType;
    }

    public void setExecTime(long execTime) {
        this.execTime = execTime;
    }

    public void setTaskType(int taskType) {
        this.taskType = taskType;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
