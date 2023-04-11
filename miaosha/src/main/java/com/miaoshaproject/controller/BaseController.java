package com.miaoshaproject.controller;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessErr;
import com.miaoshaproject.response.CommonReturnType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class BaseController {

    public static final String CONTENT_TYPE_FORMED = "application/x-www-form-urlencoded";

    //定义exceptionhandler解决未被controller层吸收的exception
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object handlerException(HttpServletRequest request, Exception e) {
        Map<String, Object> responseData = new HashMap<>();
        if(e instanceof BusinessException) {
            BusinessException businessException = (BusinessException) e;
            responseData.put("errCode", businessException.getErrCode());
            responseData.put("errMsg", businessException.getMsg());

        } else {
            responseData.put("errCode", EmBusinessErr.UNKNOW_ERROR.getErrCode());
            responseData.put("errMsg", EmBusinessErr.UNKNOW_ERROR.getMsg());
        }
        return CommonReturnType.create(responseData, "fail");
    }
}
