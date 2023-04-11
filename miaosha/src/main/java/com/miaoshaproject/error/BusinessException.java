package com.miaoshaproject.error;

//包装器业务异常类实现
public class BusinessException extends Exception implements CommonError {
    private CommonError commonError;
    //直接接收EmBusinessError的传参用于构造业务异常
    public BusinessException(CommonError commonError) {
        super();
        this.commonError = commonError;
    }

    public BusinessException(CommonError commonError, String message) {
        super();
        this.commonError = commonError;
        this.commonError.setErrMsg(message);
    }


    @Override
    public int getErrCode() {
        return this.commonError.getErrCode();
    }

    @Override
    public String getMsg() {
        return this.commonError.getMsg();
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.commonError.setErrMsg(errMsg);
        return this;
    }
}
