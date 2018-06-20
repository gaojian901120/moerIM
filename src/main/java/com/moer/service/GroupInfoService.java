package com.moer.service;

import com.moer.bean.GroupInfo;
import com.moer.dao.mysql.GroupInfoMapper;
import org.apache.ibatis.session.SqlSession;

/**
 * Created by gaoxuejian on 2018/6/20.
 */
public class GroupInfoService
{
    public GroupInfo getById(int id)
    {
        SqlSession sqlSession = ServiceFactory.getSqlSession();
        GroupInfoMapper groupInfoMapper = sqlSession.getMapper(GroupInfoMapper.class);
        return groupInfoMapper.selectByPrimaryKey(id);
    }
}
