package com.miaoshaproject.service.model;

import org.joda.time.DateTime;

import java.math.BigDecimal;

//秒杀模型
public class PromoModel {
    private Integer id;
    //秒杀活动状态 1未开始 2进行中 3已结束
    private Integer status;
    //秒杀活动名称
    private String promoName;
    //秒杀活动开始时间
    private DateTime startDate;
    //秒杀活动结束时间
    private DateTime endDate;
    //秒杀活动的适用商品
    private Integer itemId;
    //秒杀活动的商品价格
    private BigDecimal promoItemPrice;


    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 获取
     * @return id
     */
    public Integer getId() {
        return id;
    }

    /**
     * 设置
     * @param id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取
     * @return promoName
     */
    public String getPromoName() {
        return promoName;
    }

    /**
     * 设置
     * @param promoName
     */
    public void setPromoName(String promoName) {
        this.promoName = promoName;
    }

    /**
     * 获取
     * @return startDate
     */
    public DateTime getStartDate() {
        return startDate;
    }

    /**
     * 设置
     * @param startDate
     */
    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }

    /**
     * 获取
     * @return endDate
     */
    public DateTime getEndDate() {
        return endDate;
    }

    /**
     * 设置
     * @param endDate
     */
    public void setEndDate(DateTime endDate) {
        this.endDate = endDate;
    }

    /**
     * 获取
     * @return itemId
     */
    public Integer getItemId() {
        return itemId;
    }

    /**
     * 设置
     * @param itemId
     */
    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    /**
     * 获取
     * @return promoItemPrice
     */
    public BigDecimal getPromoItemPrice() {
        return promoItemPrice;
    }

    /**
     * 设置
     * @param promoItemPrice
     */
    public void setPromoItemPrice(BigDecimal promoItemPrice) {
        this.promoItemPrice = promoItemPrice;
    }

    public String toString() {
        return "PromoModel{id = " + id + ", promoName = " + promoName + ", startDate = " + startDate + ", endDate = " + endDate + ", itemId = " + itemId + ", promoItemPrice = " + promoItemPrice + "}";
    }
}
