package com.moer.thread;

import com.moer.L2ApplicationContext;
import com.moer.TimerTask;
import com.moer.common.Constant;
import com.moer.common.TraceLogger;
import com.moer.entity.ImSession;
import com.moer.entity.ImUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Created by gaoxuejian on 2018/6/25.
 */
public class TimerThread extends Thread{
    public static Logger logger = LoggerFactory.getLogger(TimerThread.class);
    public Queue<TimerTask> taskLisk = new LinkedList<>();
    @Override
    public void run() {
        Thread.currentThread().setName("TimerThread");
        while (true){
            try {

            Map<Integer,ImUser> userContext = L2ApplicationContext.getInstance().getIMUserContext();
            userContext.forEach((uid,user)-> {
                Map<String, ImSession> sessionMap = user.getSessions();
                if (sessionMap != null) {
                    sessionMap.forEach((sid, session) -> {
                        if (!session.isVaild()) {
                            L2ApplicationContext.getInstance().sessionLogout(session, "user session expired");
                            TraceLogger.trace(Constant.USER_SESSION_TRACE, "TimerThread: user {} session {} expired and logout, last active time {}", session.getUid(), session.getSeeesionId(), session.getUpdateTime());

                        }
                    });
                }
            });
                Thread.sleep(300000);
            }catch (Exception e){
                e.printStackTrace();
                logger.error("timerThread Excetion:" + e.getMessage());
            }
        }
    }

}
