package com.miaoshaproject.dao;

import com.miaoshaproject.dataobject.Promo;

public interface PromoMapper {
    int insert(Promo record);

    int insertSelective(Promo record);

    Promo selectByPrimaryKey(Integer id);

    Promo selectByItemId(Integer itemId);

    int updateByPrimaryKeySelective(Promo record);

    int updateByPrimaryKey(Promo record);
}