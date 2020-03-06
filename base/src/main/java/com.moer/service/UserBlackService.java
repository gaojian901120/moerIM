package com.moer.service;

import com.moer.bean.UserBlack;
import com.moer.bean.UserBlackExample;
import com.moer.common.MapperFactory;
import com.moer.dao.mysql.UserBlackMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaoxuejian on 2019/1/9.
 */
public class UserBlackService extends  BaseService{
    private static Logger logger = LoggerFactory.getLogger(UserBlackService.class);

    public List<UserBlack> getUserBlackList(Integer uid){
        try {
            UserBlackMapper mapper = MapperFactory.createMapper(UserBlackMapper.class,DATA_SOURCE_MOER);
            UserBlackExample example = new UserBlackExample();
            UserBlackExample.Criteria criteria = example.createCriteria();
            criteria.andInitiatorEqualTo(uid);
            return mapper.selectByExample(example);
        }catch (Exception e){
            logger.error(uid + " get blacklist exception: ",e );
            return new ArrayList<>();
        }

    }
}
