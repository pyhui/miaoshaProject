package com.miaoshaproject.dao;

import com.miaoshaproject.dataobject.Item;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ItemMapper {
    int insert(Item record);

    int insertSelective(Item record);

    Item selectByPrimaryKey(Integer id);
    //查询商品列表
    List<Item> listItem();

    int updateByPrimaryKeySelective(Item record);

    int updateByPrimaryKey(Item record);

    int increaseSales(@Param("itemId") Integer itemId, @Param("amount") Integer amount);
}