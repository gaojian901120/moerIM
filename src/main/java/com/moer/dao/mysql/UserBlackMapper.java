package com.moer.dao.mysql;

import com.moer.bean.UserBlack;
import com.moer.bean.UserBlackExample;
import com.moer.common.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserBlackMapper extends Mapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_user_black
     *
     * @mbggenerated Wed Jan 09 14:03:38 CST 2019
     */
    int countByExample(UserBlackExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_user_black
     *
     * @mbggenerated Wed Jan 09 14:03:38 CST 2019
     */
    int deleteByExample(UserBlackExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_user_black
     *
     * @mbggenerated Wed Jan 09 14:03:38 CST 2019
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_user_black
     *
     * @mbggenerated Wed Jan 09 14:03:38 CST 2019
     */
    int insert(UserBlack record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_user_black
     *
     * @mbggenerated Wed Jan 09 14:03:38 CST 2019
     */
    int insertSelective(UserBlack record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_user_black
     *
     * @mbggenerated Wed Jan 09 14:03:38 CST 2019
     */
    List<UserBlack> selectByExample(UserBlackExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_user_black
     *
     * @mbggenerated Wed Jan 09 14:03:38 CST 2019
     */
    UserBlack selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_user_black
     *
     * @mbggenerated Wed Jan 09 14:03:38 CST 2019
     */
    int updateByExampleSelective(@Param("record") UserBlack record, @Param("example") UserBlackExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_user_black
     *
     * @mbggenerated Wed Jan 09 14:03:38 CST 2019
     */
    int updateByExample(@Param("record") UserBlack record, @Param("example") UserBlackExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_user_black
     *
     * @mbggenerated Wed Jan 09 14:03:38 CST 2019
     */
    int updateByPrimaryKeySelective(UserBlack record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_user_black
     *
     * @mbggenerated Wed Jan 09 14:03:38 CST 2019
     */
    int updateByPrimaryKey(UserBlack record);
}