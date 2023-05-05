package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.PromoMapper;
import com.miaoshaproject.dataobject.Promo;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessErr;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.PromoModel;
import com.miaoshaproject.service.model.UserModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class PromoServiceImpl implements PromoService {
    @Autowired
    private PromoMapper promoMapper;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public PromoModel getPromoByIemId(Integer itemId) {
        //获取对应商品的秒杀活动信息
        Promo promo = promoMapper.selectByItemId(itemId);
        //dataobject->model
        PromoModel promoModel = convertFromDO(promo);
        if (promoModel == null) {
            return null;
        }
        //判断当前时间是否在秒杀活动时间范围内
        DateTime startDate = promoModel.getStartDate();
        DateTime endDate = promoModel.getEndDate();
        if (startDate.isAfterNow()) {
            promoModel.setStatus(1);
        } else {
            if (endDate.isAfterNow()) {
                promoModel.setStatus(2);
            } else {
                promoModel.setStatus(3);
            }
        }
        return promoModel;
    }

    @Override
    public void publishPromo(Integer promoId) {
        //通过活动id获取活动
        Promo promo = promoMapper.selectByPrimaryKey(promoId);
        if (promo == null || promo.getItemId().intValue() == 0) {
            return;
        }
        ItemModel itemModel = itemService.getItemByIdInCache(promo.getItemId());
        //将库存同步到redis内
        redisTemplate.opsForValue().set("promo_item_stock_" + itemModel.getId(),itemModel.getStock());
        //新增：将销量同步到redis内
        redisTemplate.opsForValue().set("promo_item_sales_" + itemModel.getId(),itemModel.getSales());
    }

    //生成秒杀用的令牌
    @Override
    public String generateSecondKillToken(Integer promoId, Integer itemId, Integer userId) {
        Promo promo = promoMapper.selectByPrimaryKey(promoId);
        PromoModel promoModel = convertFromDO(promo);
        if (promoModel == null) {
            return null;
        }
        //判断当前时间是否在秒杀活动时间范围内
        DateTime startDate = promoModel.getStartDate();
        DateTime endDate = promoModel.getEndDate();
        if (startDate.isAfterNow()) {
            promoModel.setStatus(1);
        } else {
            if (endDate.isAfterNow()) {
                promoModel.setStatus(2);
            } else {
                promoModel.setStatus(3);
            }
        }
        //判断活动是否正在进行
        if (promoModel.getStatus() != 2) {
            return null;
        }
        //判断item信息是否存在
        ItemModel itemModel = itemService.getItemByIdInCache(itemId);
        if (itemModel == null) {
            return null;
        }
        //判断用户信息是否存在
        UserModel userModel = userService.getUserByIdInCache(userId);
        if (userModel == null) {
            return null;
        }

        //生成token并且存入redis内
        String token = UUID.randomUUID().toString().replace("-","");

        redisTemplate.opsForValue().set("promo_token_" + promoId + "_userid_" + userId + "_itemid_" + itemId, token);
        redisTemplate.expire("promo_token_" + promoId + "_userid_" + userId + "_itemid_" + itemId, 5, TimeUnit.MINUTES);

        return token;
    }

    private PromoModel convertFromDO(Promo promo) {
        if (promo == null) {
            return null;
        }
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promo,promoModel);
        promoModel.setPromoItemPrice(new BigDecimal(promo.getPromoItemPrice()));
        promoModel.setStartDate(new DateTime(promo.getStartDate()));
        promoModel.setEndDate(new DateTime(promo.getEndDate()));
        return promoModel;
    }
}
