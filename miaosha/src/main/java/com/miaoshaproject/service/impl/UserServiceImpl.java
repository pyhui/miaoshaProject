package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.UserMapper;
import com.miaoshaproject.dao.UserPasswordMapper;
import com.miaoshaproject.dataobject.User;
import com.miaoshaproject.dataobject.UserPassword;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessErr;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import com.miaoshaproject.validator.ValidationResult;
import com.miaoshaproject.validator.ValidatorImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserPasswordMapper userPasswordMapper;

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public UserModel getUserById(Integer id) {
        //调用usermapper获取到对应用户对象
        User user = userMapper.selectByPrimaryKey(id);
        if (user == null) {
            return null;
        }
        //通过用户id获取对应用户的加密密码信息
        UserPassword userPassword = userPasswordMapper.selectByUserId(user.getId());

        return convertFromDataObject(user,userPassword);
    }

    @Override
    public UserModel getUserByIdInCache(Integer id) {
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get("user_validate_" + id);
        if (userModel == null) {
            userModel = this.getUserById(id);
            redisTemplate.opsForValue().set("user_validate_" + id,userModel);
            redisTemplate.expire("user_validate_" + id, 10, TimeUnit.MINUTES);
        }
        return userModel;
    }

    @Override
    @Transactional
    public void register(UserModel userModel) throws BusinessException {
        if (userModel == null) {
            throw new BusinessException(EmBusinessErr.PARAMETER_VALIDATION_ERROR);
        }
        //参数校验
        /*if(StringUtils.isEmpty(userModel.getName())
                || userModel.getGender() == null
                || userModel.getAge() == null
                || StringUtils.isEmpty(userModel.getTelphone())) {
            throw new BusinessException(EmBusinessErr.PARAMETER_VALIDATION_ERROR);
        }*/
        ValidationResult result = validator.validate(userModel);
        System.out.println(result);
        if(result.isHasErrors()) {
            throw new BusinessException(EmBusinessErr.PARAMETER_VALIDATION_ERROR,result.getErrMsg());
        }

        User user = convertFromModel(userModel);
        //手机号不可重复注册
        try {
            userMapper.insertSelective(user);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(EmBusinessErr.PARAMETER_VALIDATION_ERROR,"该手机号已注册");
        }
        //password表获取id
        userModel.setId(user.getId());
        UserPassword userPassword = convertPasswordFromModel(userModel);
        userPasswordMapper.insertSelective(userPassword);
    }

    @Override
    public UserModel validateLogin(String telphone, String encrptPassword) throws BusinessException {
        //通过手机号获取信息
        User user = userMapper.selectByTelphone(telphone);
        if (user == null) {
            throw new BusinessException(EmBusinessErr.USER_LOGIN_FAIL);
        }
        UserPassword userPassword = userPasswordMapper.selectByUserId(user.getId());
        UserModel userModel = convertFromDataObject(user,userPassword);
        //比对密码是否匹配
        if (!StringUtils.equals(encrptPassword,userPassword.getEncrptPassword())) {
            throw new BusinessException(EmBusinessErr.USER_LOGIN_FAIL);
        }
        return userModel;
    }

    private UserPassword convertPasswordFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserPassword userPassword = new UserPassword();
        userPassword.setEncrptPassword(userModel.getEncrptPassword());
        userPassword.setUserId(userModel.getId());
        return userPassword;
    }

    private User convertFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        User user = new User();
        BeanUtils.copyProperties(userModel,user);
        return user;
    }

    private UserModel convertFromDataObject(User user, UserPassword userPassword) {
        if (user == null) {
            return null;
        }
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(user,userModel);
        if (userPassword != null) {
            userModel.setEncrptPassword(userPassword.getEncrptPassword());
        }
        return userModel;
    }
}
