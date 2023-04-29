package com.miaoshaproject.controller;

import com.miaoshaproject.controller.viewObject.UserVO;
import com.miaoshaproject.dataobject.Order;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessErr;
import com.miaoshaproject.mq.MqProducer;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.OrderService;
import com.miaoshaproject.service.model.OrderModel;
import com.miaoshaproject.service.model.UserModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller("order")
@RequestMapping("/order")
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*",origins = "*")
public class OrderController extends BaseController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private HttpServletRequest httpServletRequest;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MqProducer mqProducer;
    @Autowired
    private ItemService itemService;

    //封装下单请求
    @RequestMapping(value = "/createorder",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam(name = "itemId") Integer itemId,
                                        @RequestParam(name = "amount") Integer amount,
                                        @RequestParam(name = "promoId", required = false) Integer promoId) throws BusinessException {
        //获取用户的登录信息
        //Boolean is_login = (Boolean) this.httpServletRequest.getSession().getAttribute("IS_LOGIN");
        String token = httpServletRequest.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token)) {
            throw new BusinessException(EmBusinessErr.USER_NOT_LOGIN,"用户未登录，不能下单");
        }
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if (userModel == null) {
            throw new BusinessException(EmBusinessErr.USER_NOT_LOGIN,"用户未登录，不能下单");
        }
        /*System.out.println("用户是否登录" + is_login);  //usercontroller是true,为什么这边一直是null
        if (is_login == null || !is_login.booleanValue()) {
            System.out.println("用户登录信息为空");
            throw new BusinessException(EmBusinessErr.USER_NOT_LOGIN,"用户未登录，不能下单");
        }
        UserModel login_user = (UserModel) this.httpServletRequest.getSession().getAttribute("LOGIN_USER");*/

        //OrderModel orderModel = orderService.createOrder(userModel.getId(), itemId, promoId, amount);
        //判断库存是否售罄，若对应的key存在，则返回下单失败
        if (redisTemplate.hasKey("promo_item_stock_invalid_" + itemId)) {
            throw new BusinessException(EmBusinessErr.STOCK_NOT_ENOUGH);
        }
        //加入库存流水init状态
        String stockLogId = itemService.initStockLog(itemId, amount);

        //在事务消息中创建订单
        if(!mqProducer.transactionAsyncReduceStock(userModel.getId(),itemId,promoId,amount,stockLogId)) {
            throw new BusinessException(EmBusinessErr.UNKNOW_ERROR,"下单失败");
        }

        return CommonReturnType.create(null);
    }

}


