package com.moer.common;

/**
 * Created by gaoxuejian on 2019/1/9.
 */

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
  * 根据mybatis.xml中配置的不同的environment创建对应的SqlSessionFactory
  */
public final class SqlSessionFactory {
    private static final Logger logger = LoggerFactory.getLogger(SqlSessionFactory.class);
    private static final String CONFIGURATION_PATH = "mybatis-conf.xml";
    private static final Map<String, org.apache.ibatis.session.SqlSessionFactory> SQLSESSIONFACTORYS = new HashMap<>();

    /**
     * 根据指定的DataSourceEnvironment获取对应的SqlSessionFactory
     */
    public static org.apache.ibatis.session.SqlSessionFactory getSqlSessionFactory(String datasource) {
        org.apache.ibatis.session.SqlSessionFactory sqlSessionFactory = SQLSESSIONFACTORYS.get(datasource);
        if (!Objects.isNull(sqlSessionFactory))
            return sqlSessionFactory;
        else {
            InputStream inputStream = null;
            try {
                inputStream = Resources.getResourceAsStream(CONFIGURATION_PATH);
                sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream, datasource);
            } catch (IOException e) {
                logger.warn("Get {} SqlSessionFactory error.", datasource);
                logger.error(e.getMessage(), e);
            } finally {
                if(inputStream!=null){
                    try {
                        inputStream.close();
                    }catch (Exception e){}
                }
            }
            SQLSESSIONFACTORYS.put(datasource, sqlSessionFactory);
             return sqlSessionFactory;
            }
    }
}

