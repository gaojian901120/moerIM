package com.moer.service;

import com.moer.bean.GroupMembers;
import com.moer.dao.mysql.GroupMembersMapper;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

/**
 * Created by gaoxuejian on 2018/6/28.
 */
public class GroupMembersService {
    public List<GroupMembers> getMember(GroupMembers groupMembers)
    {
        SqlSession sqlSession = ServiceFactory.getSqlSession();
        GroupMembersMapper groupMembersMapper = sqlSession.getMapper(GroupMembersMapper.class);
        return groupMembersMapper.selectBySelective(groupMembers);
    }
}
