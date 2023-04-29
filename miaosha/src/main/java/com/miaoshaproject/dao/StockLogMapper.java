package com.miaoshaproject.dao;

import com.miaoshaproject.dataobject.StockLog;

public interface StockLogMapper {
    int insert(StockLog record);

    int insertSelective(StockLog record);

    StockLog selectByPrimaryKey(String stockLogId);

    int updateByPrimaryKeySelective(StockLog record);

    int updateByPrimaryKey(StockLog record);
}