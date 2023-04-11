package com.miaoshaproject.error;

public interface CommonError {
    public int getErrCode();
    public String getMsg();
    public CommonError setErrMsg(String errMsg);
}
