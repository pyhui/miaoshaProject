package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.OrderMapper;
import com.miaoshaproject.dao.PromoMapper;
import com.miaoshaproject.dao.SequenceMapper;
import com.miaoshaproject.dataobject.Order;
import com.miaoshaproject.dataobject.Promo;
import com.miaoshaproject.dataobject.Sequence;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessErr;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.OrderService;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.OrderModel;
import com.miaoshaproject.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private SequenceMapper sequenceMapper;

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount) throws BusinessException {
        System.out.println("订单服务===> userId:" + userId + ",itemId:" + itemId + ",amount:" + amount);
        //校验下单状态，下单的商品是否存在，用户是否合法，购买数量是否正确
        ItemModel itemModel = itemService.getIemById(itemId);
        if (itemModel == null) {
            throw new BusinessException(EmBusinessErr.PARAMETER_VALIDATION_ERROR,"商品信息不存在");
        }
        UserModel userModel = userService.getUserById(userId);
        if (userModel == null) {
            throw new BusinessException(EmBusinessErr.PARAMETER_VALIDATION_ERROR,"用户信息不存在");
        }
        if (amount <= 0 || amount > 99) {
            throw new BusinessException(EmBusinessErr.PARAMETER_VALIDATION_ERROR,"商品数量不合法");
        }
        //校验活动信息
        if (promoId != null) {
            //校验对应活动是否存在这个适用商品
            if (promoId.intValue() != itemModel.getPromoModel().getId()) {
                throw new BusinessException(EmBusinessErr.PARAMETER_VALIDATION_ERROR,"活动信息不正确");
                //校验活动是否正在进行
            } else if (itemModel.getPromoModel().getStatus() != 2) {
                throw new BusinessException(EmBusinessErr.PARAMETER_VALIDATION_ERROR,"活动还未开始");
            }
        }
        //落单减库存
        boolean result = itemService.decreaseStock(itemId, amount);
        if (!result) {
            throw new BusinessException(EmBusinessErr.STOCK_NOT_ENOUGH);
        }
        //订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setAmount(amount);
        orderModel.setItemId(itemId);
        orderModel.setUserId(userId);
        if (promoId != null) {
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        } else {
            orderModel.setItemPrice(itemModel.getPrice());   //order中price是double，orderModel中是BigDecimal
        }
        orderModel.setPromoId(promoId);
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));
        //生成交易流水号
        orderModel.setId(generateOrderNo());
        Order order = convertFromOrderModel(orderModel); //在这转化数据格式
        orderMapper.insertSelective(order);  //数据入库
        //修改商品销量
        itemService.increaseSales(itemId,amount);
        //返回前端
        return orderModel;
    }

    //不应该添加进createOrder的事务--保证全局唯一性的策略
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateOrderNo() {
        //订单号16位
        StringBuilder stringBuilder = new StringBuilder();
        //前8位为时间信息，年月日
        LocalDateTime now = LocalDateTime.now();
        String time = now.format(DateTimeFormatter.ISO_DATE).replace("-", "");
        stringBuilder.append(time);
        //中间6位为自增序列
        //获取当前sequence
        int sequence = 0;
        Sequence s = sequenceMapper.getSequenceByName("order_info");  //数据加锁了
        sequence = s.getCurrentValue();
        s.setCurrentValue(s.getCurrentValue() + s.getStep());
        sequenceMapper.updateByPrimaryKeySelective(s);
        //凑足6位。补零
        String sequenceStr = String.valueOf(sequence);
        for (int i = 0; i < 6 - sequenceStr.length(); i++) {
            stringBuilder.append(0);
        }
        stringBuilder.append(sequenceStr);
        //最后2位为分库分表位，暂时写死
        stringBuilder.append("00");
        System.out.println("交易流水号===>" + stringBuilder.toString());
        return stringBuilder.toString();
    }

    private Order convertFromOrderModel(OrderModel orderModel) {
        if (orderModel == null) {
            return null;
        }
        Order order = new Order();
        BeanUtils.copyProperties(orderModel,order);
        order.setItemPrice(orderModel.getItemPrice().doubleValue());  //忘了转化数据格式，导致price数据没存进库
        order.setOrderPrice(orderModel.getOrderPrice().doubleValue());
        return order;
    }
}
