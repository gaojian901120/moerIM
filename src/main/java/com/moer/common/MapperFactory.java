package com.moer.common;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by gaoxuejian on 2019/1/9.
 */
public class MapperFactory{
    private static final Logger logger = LoggerFactory.getLogger(MapperFactory.class);

    @SuppressWarnings("unchecked")
    public static  <T> T createMapper(Class<? extends Mapper> clazz, String dataSource){
        org.apache.ibatis.session.SqlSessionFactory sqlSessionFactory = getSqlSessionFactory(dataSource);
        SqlSession sqlSession = SqlSessionManager.newInstance(sqlSessionFactory);
        Mapper mapper = sqlSession.getMapper(clazz);
        return (T)mapper;
    }

    /**
     * 获取数据源 datasource 的 SqlSessionFactory
     */
    private static org.apache.ibatis.session.SqlSessionFactory getSqlSessionFactory(String datasource) {
        return SqlSessionFactory.getSqlSessionFactory(datasource);
    }
}
