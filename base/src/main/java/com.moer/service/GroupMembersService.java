package com.moer.service;

import com.moer.bean.GroupMembers;
import com.moer.common.MapperFactory;
import com.moer.dao.mysql.GroupMembersMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaoxuejian on 2018/6/28.
 */
public class GroupMembersService extends BaseService{
    private static Logger logger = LoggerFactory.getLogger(GroupMembers.class);
    public List<GroupMembers> getMember(GroupMembers groupMembers)
    {
        try {
            GroupMembersMapper groupMembersMapper = MapperFactory.createMapper(GroupMembersMapper.class,DATA_SOURCE_LIVE);
            return groupMembersMapper.selectBySelective(groupMembers);
        }catch (Exception e){
            logger.error(groupMembers.getUid() +" get members exception: ",e);
            return new ArrayList<>();
        }

    }
}
