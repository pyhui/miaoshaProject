package com.miaoshaproject.controller;

import com.alibaba.druid.util.StringUtils;
import com.miaoshaproject.controller.viewObject.UserVO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessErr;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
//import sun.misc.BASE64Encoder;
import org.apache.tomcat.util.codec.binary.Base64;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;


@Controller("user")
@RequestMapping("/user")
//@CrossOrigin
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*",origins = "*")  //跨域  , originPatterns = "*"
public class UserController extends BaseController {
    @Autowired
    private UserService userService;

    //通过Spring Bean包装的httpServletRequest，
    // 它的内部拥有ThreadLocal方式的map，去让用户在每个线程中处理自己对应的request，
    // 并且有ThreadLocal清除的机制。
    @Autowired
    private HttpServletRequest httpServletRequest;
    //解决谷歌禁用Cookie
    @Autowired
    private HttpServletResponse httpServletResponse;

    //用户登录接口
    @RequestMapping(value = "/login",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType login(@RequestParam(name = "telphone") String telphone,
                                  @RequestParam(name = "password") String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //入参校验
        if (StringUtils.isEmpty(telphone) || StringUtils.isEmpty(password)) {
            throw new BusinessException(EmBusinessErr.PARAMETER_VALIDATION_ERROR,"用户名或密码不能为空");
        }
        //用户登录服务，用来校验用户登录是否合法   //密码加密
        UserModel userModel = userService.validateLogin(telphone, this.EncodeByMd5(password));
        //将登陆凭证加入到用户登陆成功的session内

        // 解决Google cookie 问题
        //设置samesite=None, httponly,secure等属性
       /* ResponseCookie cookie = ResponseCookie.from("JSESSIONID", httpServletRequest.getSession().getId() ) // key & value
                .httpOnly(true)       // 禁止js读取
                .secure(true)     // 在http下也传输
                .domain("localhost")// 域名
                .path("/")       // path
                .maxAge(3600)  // 1个小时后过期
                .sameSite("None")  // 大多数情况也是不发送第三方 Cookie，但是导航到目标网址的 Get 请求除外
                .build();
        httpServletResponse.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());*/

        this.httpServletRequest.getSession().setAttribute("IS_LOGIN",true); //登陆了仍然为null???加了上面的cookie
        this.httpServletRequest.getSession().setAttribute("LOGIN_USER",userModel);
        return CommonReturnType.create(null);
    }
    //用户注册接口
    @RequestMapping(value = "/register",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType register(@RequestParam(name = "telphone") String telphone,
                                     @RequestParam(name = "otpCode") String otpCode,
                                     @RequestParam(name = "name") String name,
                                     @RequestParam(name = "gender") Integer gender,
                                     @RequestParam(name = "age") String age,
                                     @RequestParam(name = "password") String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //验证手机号和验证码相符合
        String inSessionOtpCode = (String) this.httpServletRequest.getSession().getAttribute(telphone);
        if (!com.alibaba.druid.util.StringUtils.equals(otpCode, inSessionOtpCode)) {  //会先做非空判断
            throw new BusinessException(EmBusinessErr.PARAMETER_VALIDATION_ERROR, "短信验证码不符合");
        }
        //用户注册流程
        UserModel userModel = new UserModel();
        userModel.setTelphone(telphone);
        userModel.setName(name);
        userModel.setGender(new Byte(String.valueOf(gender.intValue())));
        userModel.setAge(Integer.parseInt(age));
        userModel.setTelphone(telphone);
        userModel.setRegisterMode("byPhone");
        userModel.setEncrptPassword(this.EncodeByMd5(password)); //加密存到数据库中
        System.out.println("用户注册信息" + userModel);
        userService.register(userModel);
        return CommonReturnType.create(null);
    }

    //加密
    public String EncodeByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //确定计算方法
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        /*BASE64Encoder base64Encoder = new BASE64Encoder();
        //加密字符串
        String newStr = base64Encoder.encode(md5.digest(str.getBytes("utf-8")));*/
        String newStr = Base64.encodeBase64String(md5.digest(str.getBytes("utf-8")));
        return newStr;
    }

    //用户获取otp短信接口
    @RequestMapping(value = "/getotp",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam(name = "telphone") String telphone) {
        //需要按照一定的规则生成OTP验证码
        Random random = new Random();
        int randonInt = random.nextInt(99999);
        randonInt += 10000;
        String optCode = String.valueOf(randonInt);
        // 解决Google cookie 问题
        //设置samesite=None, httponly,secure等属性
        /*
        ResponseCookie cookie = ResponseCookie.from("JSESSIONID", httpServletRequest.getSession().getId() ) // key & value
                .httpOnly(true)       // 禁止js读取
                .secure(true)     // 在http下也传输
                .domain("localhost")// 域名
                .path("/")       // path
                .maxAge(3600)  // 1个小时候过期
                .sameSite("None")  // 大多数情况也是不发送第三方 Cookie，但是导航到目标网址的 Get 请求除外
                .build();
        httpServletResponse.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        */
        //将OTP验证码同对应用户的手机号关联，使用httpsession的方式绑定他的手机号与OTPCODE
        httpServletRequest.getSession().setAttribute(telphone,optCode);
        //将OTP验证码通过短信通道发送给用户，省略
        System.out.println("telphone = " + telphone + " & otpCode = " + optCode);

        return CommonReturnType.create(null);
    }

    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name="id") Integer id) throws BusinessException {
        //调用service服务获取对应id的用户对象并返回给前端
        UserModel userModel = userService.getUserById(id);
        //若获取的用户信息不存在
        if (userModel == null) {
            throw new BusinessException(EmBusinessErr.USER_NOT_EXIST);
        }
        //对核心领域模型用户对象转化为可供前端使用的viewobject
        UserVO userVO = convertFromModel(userModel);
        //返回通用对象
        return CommonReturnType.create(userVO);
    }

    private UserVO convertFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel,userVO);
        return userVO;
    }

}
