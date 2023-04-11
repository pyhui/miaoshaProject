package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.UserModel;

public interface UserService {
    UserModel getUserById(Integer id);

    void register(UserModel userModel) throws BusinessException;

    /**
     * @param telphone 用户注册手机号
     * @param encrptPassword 用户输入的密码（加密后）
     * @throws BusinessException
     */
    UserModel validateLogin(String telphone, String encrptPassword) throws BusinessException;
}
