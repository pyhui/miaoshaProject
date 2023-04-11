package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.PromoMapper;
import com.miaoshaproject.dataobject.Promo;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.model.PromoModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PromoServiceImpl implements PromoService {
    @Autowired
    private PromoMapper promoMapper;

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
