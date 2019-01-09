package com.moer.service;

import com.moer.bean.UserBlack;
import com.moer.bean.UserBlackExample;
import com.moer.common.MapperFactory;
import com.moer.dao.mysql.UserBlackMapper;

import java.util.List;

/**
 * Created by gaoxuejian on 2019/1/9.
 */
public class UserBlackService extends  BaseService{
    public List<UserBlack> getUserBlackList(Integer uid){
        UserBlackMapper mapper = MapperFactory.createMapper(UserBlackMapper.class,DATA_SOURCE_MOER);
        UserBlackExample example = new UserBlackExample();
        UserBlackExample.Criteria criteria = example.createCriteria();
        criteria.andInitiatorEqualTo(uid);
        return mapper.selectByExample(example);
    }
}
