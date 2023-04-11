package com.miaoshaproject.service.model;


import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class UserModel {
    private Integer id;
    @NotBlank(message = "用户名不能为空（注解）")   //org.hibernate.validator.constraints 能运行
//    @NotBlank(message = "用户名不能为空")  //javax.validation.constraints  不能运行，显示未知错误
    private String name;
    @NotNull(message = "性别不能不填（注解）")
    private Byte gender;
    @NotNull(message = "年龄不能不填（注解）")
    @Min(value = 0,message = "年龄必须大于0（注解）")
    @Max(value = 150,message = "年龄必须小于150（注解）")
    private Integer age;
    @NotBlank(message = "手机号不能为空（注解）")
    private String telphone;

    private String registerMode;

    private String thirdPartyId;
    @NotBlank(message = "密码不能为空")
    private String encrptPassword;


    public UserModel() {
    }

    public UserModel(Integer id, String name, Byte gender, Integer age, String telphone, String registerMode, String thirdPartyId, String encrptPassword) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.telphone = telphone;
        this.registerMode = registerMode;
        this.thirdPartyId = thirdPartyId;
        this.encrptPassword = encrptPassword;
    }

    /**
     * 获取
     * @return id
     */
    public Integer getId() {
        return id;
    }

    /**
     * 设置
     * @param id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * 设置
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取
     * @return gender
     */
    public Byte getGender() {
        return gender;
    }

    /**
     * 设置
     * @param gender
     */
    public void setGender(Byte gender) {
        this.gender = gender;
    }

    /**
     * 获取
     * @return age
     */
    public Integer getAge() {
        return age;
    }

    /**
     * 设置
     * @param age
     */
    public void setAge(Integer age) {
        this.age = age;
    }

    /**
     * 获取
     * @return telphone
     */
    public String getTelphone() {
        return telphone;
    }

    /**
     * 设置
     * @param telphone
     */
    public void setTelphone(String telphone) {
        this.telphone = telphone;
    }

    /**
     * 获取
     * @return registerMode
     */
    public String getRegisterMode() {
        return registerMode;
    }

    /**
     * 设置
     * @param registerMode
     */
    public void setRegisterMode(String registerMode) {
        this.registerMode = registerMode;
    }

    /**
     * 获取
     * @return thirdPartyId
     */
    public String getThirdPartyId() {
        return thirdPartyId;
    }

    /**
     * 设置
     * @param thirdPartyId
     */
    public void setThirdPartyId(String thirdPartyId) {
        this.thirdPartyId = thirdPartyId;
    }

    /**
     * 获取
     * @return encrptPassword
     */
    public String getEncrptPassword() {
        return encrptPassword;
    }

    /**
     * 设置
     * @param encrptPassword
     */
    public void setEncrptPassword(String encrptPassword) {
        this.encrptPassword = encrptPassword;
    }

    public String toString() {
        return "UserModel{id = " + id + ", name = " + name + ", gender = " + gender + ", age = " + age + ", telphone = " + telphone + ", registerMode = " + registerMode + ", thirdPartyId = " + thirdPartyId + ", encrptPassword = " + encrptPassword + "}";
    }
}
