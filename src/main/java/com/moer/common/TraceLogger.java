package com.moer.common;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaoxuejian on 2019/1/14.
 */
public class TraceLogger {
    private static Map<String, Logger> loggerMap = new HashMap<>();
    private static Logger getLogger(String name2){
        if(loggerMap.containsKey(name2)){
            return loggerMap.get(name2);
        }else {
            Logger logger = LoggerFactory.getLogger(name2);
            loggerMap.put(name2, logger);
            return logger;
        }
    }

    public static void trace(Marker marker,String format, Object ... arg){
        String name = marker.getName();
        Logger logger = getLogger(name);
        logger.trace(format, arg);
    }
}
