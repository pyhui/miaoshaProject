package com.miaoshaproject.dataobject;

public class StockLog {
    private String stockLogId;

    private Integer itemId;

    private Integer amount;

    private Integer state;

    public String getStockLogId() {
        return stockLogId;
    }

    public void setStockLogId(String stockLogId) {
        this.stockLogId = stockLogId == null ? null : stockLogId.trim();
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }
}