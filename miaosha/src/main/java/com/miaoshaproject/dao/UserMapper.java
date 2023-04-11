package com.miaoshaproject.dao;

import com.miaoshaproject.dataobject.User;

public interface UserMapper {
    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    User selectByTelphone(String telphone);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);
}