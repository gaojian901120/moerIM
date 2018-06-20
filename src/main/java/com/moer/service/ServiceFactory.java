package com.moer.service;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.session.SqlSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;

/**
 * Created by gaoxuejian on 2018/6/20.
 */
public class ServiceFactory
{
    private static SqlSessionFactory sessionFactory;
    private static Logger logger = LoggerFactory.getLogger(ServiceFactory.class);
    private static Map<String,Object> serviceMap = new HashMap<>();
    public static void init() {
        try {
            sessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsStream("mybatis-conf.xml"));

        } catch (Exception e) {
            logger.error("init mybaits conf failed with excetion:" + e.getMessage());
        }
    }
    public static  <T> T getInstace(Class T)
    {
        if (serviceMap.containsKey(T.getName())){
            return (T)serviceMap.get(T.getName());
        }
        synchronized (serviceMap){
            if (serviceMap.containsKey(T.getName())){
                return (T)serviceMap.get(T.getName());
            }
            try {
                T t = (T)T.newInstance();
                serviceMap.put(T.getName(), t);
                return t;
            }catch (Exception e){
                logger.error("get instance {} failed",T.getName());
                return null;
            }
        }
    }

    public static SqlSession getSqlSession()
    {
        //@TODO
        // 通过SqlSessionManager获取到的sqlsession 是线程安全的 因为service是单利 但是必须保证里面使用到的连接是线程安全的 包括redis 和其他网络连接
        return SqlSessionManager.newInstance(sessionFactory);
    }
}
