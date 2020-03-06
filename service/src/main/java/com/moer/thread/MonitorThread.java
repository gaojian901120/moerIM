package com.moer.thread;

import com.alibaba.fastjson.JSON;
import com.moer.L2ApplicationContext;
import com.moer.L2ServiceApplication;
import com.moer.common.Constant;
import com.moer.common.TraceLogger;
import io.netty.util.internal.PlatformDependent;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

/**
 * Created by gaoxuejian on 2019/2/22.
 */
public class MonitorThread extends Thread {
    private long lastMailedTime = 0;
    @Override
    public void run() {
        Thread.currentThread().setName("MonitorThread");
        while (true){
            try {
                TimerThread timerThread = L2ApplicationContext.getInstance().timerThread;
                SubscribeThread subscribeThread = L2ApplicationContext.getInstance().subscribeThread;
                DataSyncToRedisThread dataSyncToRedisThread = L2ApplicationContext.getInstance().dataSyncToRedisThread;
                PushThreadPool pushThreadPool = L2ApplicationContext.getInstance().pushThreadPool;
                checkThreadState(timerThread);
                checkThreadState(subscribeThread);
                checkThreadState(dataSyncToRedisThread);
                PushThreadPool.PushThread [] pts = pushThreadPool.getPushThreads();
                for (PushThreadPool.PushThread pt : pts){
                    checkThreadState(pt);
                }
                Map<Integer,Integer> pushStatus = pushThreadPool.getPushStatus();
                checkThreadState(this);
                //查看TimerThread中的当前任务数量
                int timerTaskSize = timerThread.taskLisk.size();
                TraceLogger.trace(Constant.MONITOR_TRACE,"pushThreadPool 任务数量：" + JSON.toJSONString(pushStatus));
                String status = JSON.toJSONString(L2ApplicationContext.getInstance().imConfig);
                status += "<br>";
                status += "DispatchServer 执行信息：" + DispatchServer.getState();
                status += "<br>";
                status += "timerThread 任务数量："+timerTaskSize;
                status += "<br>";
                TraceLogger.trace(Constant.MONITOR_TRACE,status);
                Field [] fields = PlatformDependent.class.getDeclaredFields();
                for (Field field : fields){
                    field.setAccessible(true);
                    if (field.getName().equals("DIRECT_MEMORY_COUNTER")){
                        TraceLogger.trace(Constant.MONITOR_TRACE,"netty direct memory:" + field.get(PlatformDependent.class));

                    }
                }
                boolean mailed = false;
                for (Map.Entry<Integer,Integer> item : pushStatus.entrySet()){
                    if (item.getKey() >= 1000){
                        mailed = true;
                        break;
                    }
                }
                if(DispatchServer.getQueued() > 1000 || mailed) {//待下发消息数大于1000 则邮件报警
                    long curTime = System.currentTimeMillis();
                    if(lastMailedTime == 0 || curTime-lastMailedTime>300000) {//五分钟报警一次
                        lastMailedTime = curTime;
                        Properties props = new Properties();                // 用于连接邮件服务器的参数配置（发送邮件时才需要用到）
                        props.setProperty("mail.transport.protocol", "smtp");
                        props.setProperty("mail.smtp.host", "smtp.exmail.qq.com");
                        Session session = Session.getInstance(props);        // 根据参数配置，创建会话对象（为了发送邮件准备的）
                        MimeMessage message = new MimeMessage(session);     // 创建邮件对象
                        L2ServiceApplication.SingleHandler handler = new L2ServiceApplication.SingleHandler();
                        message.setFrom(new InternetAddress("gaoxuejian@jiemian.com", "MOER_IM", "UTF-8"));
                        message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress("gaoxuejian@jiemian.com", "高雪建", "UTF-8"));
                        message.setSubject("摩尔IM服务监控报警", "UTF-8");
                        message.setContent(status, "text/html;charset=UTF-8");
                        session.setDebug(true);

                        // 6. 设置显示的发件时间
                        message.setSentDate(new Date());
                        // 7. 保存前面的设置
                        message.saveChanges();
                        Transport transport = session.getTransport();
                        transport.connect("gaoxuejian@jiemian.com", "Gaojian901120");
                        transport.sendMessage(message, message.getAllRecipients());
                        transport.close();
                    }
                }
                writeToFile("/data/logs/ol-user.log",L2ApplicationContext.getInstance().getIMUserContext().size()+"");
                Thread.sleep(30000);
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
//            thread.start();
//            threadInfo = threadInfo + "Restart thread because it is terminated";
        }
        TraceLogger.trace(Constant.MONITOR_TRACE,threadInfo);
    }
    public static void writeToFile(String fileName, String content) {
        try {
            //打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            FileWriter writer = new FileWriter(fileName, false);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

