package com.miaoshaproject.controller.viewObject;

public class UserVO {
    private Integer id;

    private String name;

    private Byte gender;

    private Integer age;

    private String telphone;


    public UserVO() {
    }

    public UserVO(Integer id, String name, Byte gender, Integer age, String telphone) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.telphone = telphone;
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

    public String toString() {
        return "UserVO{id = " + id + ", name = " + name + ", gender = " + gender + ", age = " + age + ", telphone = " + telphone + "}";
    }
}
