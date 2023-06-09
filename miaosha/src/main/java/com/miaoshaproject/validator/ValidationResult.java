package com.miaoshaproject.validator;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class ValidationResult {
    //校验结果是否有错
    private boolean hasErrors = false;

    //存放错误信息的map
    private Map<String,String> errMsgMap = new HashMap<>();

    public boolean isHasErrors() {
        return hasErrors;
    }

    public void setHasErrors(boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

    public Map<String, String> getErrMsgMap() {
        return errMsgMap;
    }

    public void setErrMsgMap(Map<String, String> errMsgMap) {
        this.errMsgMap = errMsgMap;
    }

    //实现通用的通过格式化字符信息获取错误结果的msg方法
    public String getErrMsg() {
        return StringUtils.join(errMsgMap.values().toArray(),",");
    }

    @Override
    public String toString() {
        return "ValidationResult{" +
                "hasErrors=" + hasErrors +
                ", errMsgMap=" + errMsgMap +
                '}';
    }
}
