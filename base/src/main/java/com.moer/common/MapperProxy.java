package com.moer.common;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


/**
 * Created by gaoxuejian on 2019/1/9.
 */
class MapperProxy implements InvocationHandler {
    private static final Logger logger = LoggerFactory.getLogger(MapperProxy.class);

    private Mapper mapper;
    private SqlSession sqlSession;

    private MapperProxy(Mapper mapper, SqlSession sqlSession) {
        this.mapper = mapper;
        this.sqlSession = sqlSession;
    }

    public static Mapper bind(Mapper mapper, SqlSession sqlSession) {
        return (Mapper) Proxy.newProxyInstance(mapper.getClass().getClassLoader(),
                mapper.getClass().getInterfaces(), new MapperProxy(mapper, sqlSession));
    }
    /**
     * 执行mapper方法并最终关闭sqlSession
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object object = null;
        try {
            object = method.invoke(mapper, args);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        } finally {
            sqlSession.close();
        }
        return object;
    }
}
