package com.miaoshaproject.controller;

import com.miaoshaproject.Util.CodeUtil;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessErr;
import com.miaoshaproject.mq.MqProducer;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.OrderService;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.model.UserModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

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
    @Autowired
    private PromoService promoService;

    private ExecutorService executorService;

    @PostConstruct
    public void init() {
        executorService = Executors.newFixedThreadPool(20);
    }

    //生成验证码
    @RequestMapping(value = "/generateverifycode",method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public void generateVerifyCode(HttpServletResponse response) throws BusinessException, IOException {
        //验证用户登录态
        String token = httpServletRequest.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token)) {
            throw new BusinessException(EmBusinessErr.USER_NOT_LOGIN,"用户未登录，不能生成验证码");
        }
        //获取用户的登录信息
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if (userModel == null) {
            throw new BusinessException(EmBusinessErr.USER_NOT_LOGIN,"用户未登录，不能生成验证码");
        }

        Map<String, Object> map = CodeUtil.generateCodeAndPic();

        redisTemplate.opsForValue().set("verify_code_" + userModel.getId(), map.get("code"));
        redisTemplate.expire("verify_code_" + userModel.getId(), 10, TimeUnit.MINUTES);

        ImageIO.write((RenderedImage) map.get("codePic"), "jpeg", response.getOutputStream());
        System.out.println("验证码的值为：" + map.get("code"));
    }



    //生成秒杀令牌
    @RequestMapping(value = "/generatetoken",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType generateToken(@RequestParam(name = "itemId") Integer itemId,
                                          @RequestParam(name = "promoId") Integer promoId,
                                          @RequestParam(name = "verifyCode") String verifyCode) throws BusinessException {
        //根据token获取用户信息
        String token = httpServletRequest.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token)) {
            throw new BusinessException(EmBusinessErr.USER_NOT_LOGIN,"用户未登录，不能下单");
        }
        //获取用户的登录信息
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if (userModel == null) {
            throw new BusinessException(EmBusinessErr.USER_NOT_LOGIN,"用户未登录，不能下单");
        }

        //通过verifycode验证验证码的有效性
        String redisVerifyCode = (String) redisTemplate.opsForValue().get("verify_code_" + userModel.getId());
        if (!StringUtils.equalsIgnoreCase(redisVerifyCode,verifyCode)) {
            throw new BusinessException(EmBusinessErr.PARAMETER_VALIDATION_ERROR,"验证码错误");
        }

        //获取秒杀访问令牌
        String promoToken = promoService.generateSecondKillToken(promoId, itemId, userModel.getId());
        if (promoToken == null) {
            throw new BusinessException(EmBusinessErr.PARAMETER_VALIDATION_ERROR,"生成令牌失败");
        }
        //返回对应的结果
        return CommonReturnType.create(promoToken);
    }

    //封装下单请求
    @RequestMapping(value = "/createorder",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam(name = "itemId") Integer itemId,
                                        @RequestParam(name = "amount") Integer amount,
                                        @RequestParam(name = "promoId", required = false) Integer promoId,
                                        @RequestParam(name = "promoToken", required = false) String promoToken) throws BusinessException {
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

        //校验秒杀令牌是否正确
        if (promoToken != null) {
            String inRedisPromoToken = (String) redisTemplate.opsForValue().get("promo_token_" + promoId + "_userid_" + userModel.getId() + "_itemid_" + itemId);
            if (inRedisPromoToken == null) {
                throw new BusinessException(EmBusinessErr.PARAMETER_VALIDATION_ERROR,"秒杀令牌校验失败");
            }
            if (!StringUtils.equals(promoToken,inRedisPromoToken)) {
                throw new BusinessException(EmBusinessErr.PARAMETER_VALIDATION_ERROR,"秒杀令牌校验失败");
            }
        }

        /*System.out.println("用户是否登录" + is_login);  //usercontroller是true,为什么这边一直是null
        if (is_login == null || !is_login.booleanValue()) {
            System.out.println("用户登录信息为空");
            throw new BusinessException(EmBusinessErr.USER_NOT_LOGIN,"用户未登录，不能下单");
        }
        UserModel login_user = (UserModel) this.httpServletRequest.getSession().getAttribute("LOGIN_USER");*/

        //OrderModel orderModel = orderService.createOrder(userModel.getId(), itemId, promoId, amount);
        /*//判断库存是否售罄，若对应的key存在，则返回下单失败 (前置到PromoService.generateSecondKillToken)
        if (redisTemplate.hasKey("promo_item_stock_invalid_" + itemId)) {
            throw new BusinessException(EmBusinessErr.STOCK_NOT_ENOUGH);
        }*/

        //同步调用线程池的submit方法
        //拥塞窗口为20的等待队列，用来队列泄洪
        Future<Object> future = executorService.submit(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                //加入库存流水init状态
                String stockLogId = itemService.initStockLog(itemId, amount);

                //在事务消息中创建订单
                if(!mqProducer.transactionAsyncReduceStock(userModel.getId(),itemId,promoId,amount,stockLogId)) {
                    throw new BusinessException(EmBusinessErr.UNKNOW_ERROR,"下单失败");
                }
                return null;
            }
        });

        try {
            future.get();
        } catch (InterruptedException e) {
            throw new BusinessException(EmBusinessErr.UNKNOW_ERROR);
        } catch (ExecutionException e) {
            throw new BusinessException(EmBusinessErr.UNKNOW_ERROR);
        }

        return CommonReturnType.create(null);
    }

}


