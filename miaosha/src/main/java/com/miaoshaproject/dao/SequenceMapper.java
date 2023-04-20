package com.miaoshaproject.dao;

import com.miaoshaproject.dataobject.Sequence;

public interface SequenceMapper {
    int insert(Sequence record);

    int insertSelective(Sequence record);

    Sequence selectByPrimaryKey(String name);

    Sequence getSequenceByName(String name);

    int updateByPrimaryKeySelective(Sequence record);

    int updateByPrimaryKey(Sequence record);
}