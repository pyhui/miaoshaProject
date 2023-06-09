package com.miaoshaproject.dao;

import com.miaoshaproject.dataobject.UserPassword;

public interface UserPasswordMapper {
    int insert(UserPassword record);

    int insertSelective(UserPassword record);

    UserPassword selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UserPassword record);

    int updateByPrimaryKey(UserPassword record);

    UserPassword selectByUserId(Integer user_id);
}