package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.ItemMapper;
import com.miaoshaproject.dao.ItemStockMapper;
import com.miaoshaproject.dao.StockLogMapper;
import com.miaoshaproject.dataobject.Item;
import com.miaoshaproject.dataobject.ItemStock;
import com.miaoshaproject.dataobject.StockLog;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessErr;
import com.miaoshaproject.mq.MqProducer;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.PromoModel;
import com.miaoshaproject.validator.ValidationResult;
import com.miaoshaproject.validator.ValidatorImpl;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private ItemStockMapper itemStockMapper;

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private PromoService promoService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MqProducer mqProducer;

    @Autowired
    private StockLogMapper stockLogMapper;

    //创建商品
    @Override
    @Transactional
    public ItemModel createItem(ItemModel itemModel) throws BusinessException {
        //校验入参
        ValidationResult result = validator.validate(itemModel);
        if (result.isHasErrors()) {
            throw new BusinessException(EmBusinessErr.PARAMETER_VALIDATION_ERROR,result.getErrMsg());
        }
        //转化itemModel->dataobject
        Item item = this.convertItemFromItemModel(itemModel);
        //写入数据库
        itemMapper.insertSelective(item);
        itemModel.setId(item.getId()); //获取id
        ItemStock itemStock = this.convertItemStockFromItemModel(itemModel);
        itemStockMapper.insertSelective(itemStock);  //itemStock入库

        //返回创建完成的对象
        return this.getIemById(itemModel.getId()); //不懂视频里为什么要通过getItemById获取
        //懂了，需要去数据库里面取值，通过controller层返回结果给前端
    }
    //商品列表浏览
    @Override
    public List<ItemModel> listItem() {
        List<Item> itemList = itemMapper.listItem();
        //stream流将itemList转化成itemModelList
        List<ItemModel> itemModelList = itemList.stream().map(item -> {
            ItemModel itemModel = this.convertItemModelFromDO(item, itemStockMapper.selectByItemId(item.getId()));
            return itemModel;
        }).collect(Collectors.toList());
        return itemModelList;
    }
    //商品详情浏览
    @Override
    public ItemModel getIemById(Integer id) {
        Item item = itemMapper.selectByPrimaryKey(id);
        if (item == null) {
            return null;
        }
        //操作获得库存数量
        ItemStock itemStock = itemStockMapper.selectByItemId(item.getId());
        //dataobject->model
        ItemModel itemModel = this.convertItemModelFromDO(item, itemStock);
        //获取活动信息
        PromoModel promoModel = promoService.getPromoByIemId(item.getId());
        if (promoModel != null && promoModel.getStatus().intValue() != 3) {
            //有活动
            itemModel.setPromoModel(promoModel);
        }
        return itemModel;
    }

    //item及promo model缓存模型
    @Override
    public ItemModel getItemByIdInCache(Integer id) {
        ItemModel itemModel = (ItemModel) redisTemplate.opsForValue().get("item_validate_" + id);
        if (itemModel == null) {
            itemModel = this.getIemById(id);
            redisTemplate.opsForValue().set("item_validate_" + id, itemModel);
            redisTemplate.expire("item_validate_" + id, 10, TimeUnit.MINUTES);
        }
        return itemModel;
    }

    @Override
    @Transactional
    public boolean decreaseStock(Integer itemId, Integer amount) {
        //int i = itemStockMapper.decreaseStock(itemId, amount);
        Long result = redisTemplate.opsForValue().increment("promo_item_stock_" + itemId, amount.intValue() * -1);
        if (result > 0) {
            //更新库存成功
            return true;
        } else if (result == 0) {
            //打上库存售罄标识
            redisTemplate.opsForValue().set("promo_item_stock_invalid_" + itemId, "true");
            //更新库存成功
            return true;
        }else {
            //更新库存失败
            increaseStock(itemId,amount);
            return false;
        }
    }

    @Override
    public boolean increaseStock(Integer itemId, Integer amount) {
        redisTemplate.opsForValue().increment("promo_item_stock_" + itemId, amount.intValue());
        return true;
    }

    @Override
    public boolean asyncDecreaseStock(Integer itemId, Integer amount) {
        boolean mqResult = mqProducer.asyncReduceStock(itemId, amount);
        return mqResult;
    }

    @Override
    @Transactional
    public boolean increaseSales(Integer id, Integer amount) {
        int i = itemMapper.increaseSales(id, amount);
        return i > 0 ? true : false;
    }

    //初始化库存流水
    @Override
    @Transactional
    public String initStockLog(Integer itemId, Integer amount) {
        StockLog stockLog = new StockLog();
        stockLog.setAmount(amount);
        stockLog.setItemId(itemId);
        stockLog.setStockLogId(UUID.randomUUID().toString().replace("-",""));
        //1表示初始状态，2表示下单扣减库存成功，3表示下单失败
        stockLog.setState(1);
        stockLogMapper.insertSelective(stockLog);
        return stockLog.getStockLogId();
    }

    private ItemStock convertItemStockFromItemModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemStock itemStock = new ItemStock();
        itemStock.setItemId(itemModel.getId());
        itemStock.setStock(itemModel.getStock());
        return itemStock;
    }
    private Item convertItemFromItemModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        Item item = new Item();
        BeanUtils.copyProperties(itemModel,item);
        //bigdecimal转化为double，因为直接传double到前端会丢失精度
        item.setPrice(itemModel.getPrice().doubleValue());
        return item;
    }

    private ItemModel convertItemModelFromDO(Item item, ItemStock itemStock) {
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(item,itemModel);
        itemModel.setPrice(new BigDecimal(item.getPrice())); //item的price是double，itemModel是BigDecimal
        itemModel.setStock(itemStock.getStock());
        return itemModel;
    }
}
