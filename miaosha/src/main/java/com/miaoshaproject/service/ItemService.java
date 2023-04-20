package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.ItemModel;

import java.util.List;

public interface ItemService {
    //创建商品
    ItemModel createItem(ItemModel itemModel) throws BusinessException;
    //商品列表浏览
    List<ItemModel> listItem();
    //商品详情浏览
    ItemModel getIemById(Integer id);
    //库存扣减
    boolean decreaseStock(Integer itemId, Integer amount) throws BusinessException;
    //增加销量
    boolean increaseSales(Integer itemId, Integer amount) throws BusinessException;
}
