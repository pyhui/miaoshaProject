package com.miaoshaproject.dao;

import com.miaoshaproject.dataobject.ItemStock;
import org.apache.ibatis.annotations.Param;

public interface ItemStockMapper {
    int insert(ItemStock record);

    int insertSelective(ItemStock record);

    ItemStock selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ItemStock record);

    int updateByPrimaryKey(ItemStock record);

    ItemStock selectByItemId(Integer itemId);

    int decreaseStock(@Param("itemId") Integer itemId, @Param("amount") Integer amount);
}