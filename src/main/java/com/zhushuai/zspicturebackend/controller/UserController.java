package com.zhushuai.zspicturebackend.controller;


import com.zhushuai.zspicturebackend.annotation.AuthCheck;
import com.zhushuai.zspicturebackend.common.BaseResponse;
import com.zhushuai.zspicturebackend.common.ResultUtils;
import com.zhushuai.zspicturebackend.constant.UserConstant;
import com.zhushuai.zspicturebackend.exception.ErrorCode;
import com.zhushuai.zspicturebackend.exception.ThrowUtils;
import com.zhushuai.zspicturebackend.model.dto.user.UserAddRequest;
import com.zhushuai.zspicturebackend.model.dto.user.UserLoginRequest;
import com.zhushuai.zspicturebackend.model.dto.user.UserRegisterRequest;
import com.zhushuai.zspicturebackend.model.entity.User;
import com.zhushuai.zspicturebackend.model.vo.LoginUserVO;
import com.zhushuai.zspicturebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR, "参数为空");

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();

        long res = userService.userRegister(userAccount, userPassword, checkPassword);

        return ResultUtils.success(res);
    }

    /**
     * 用户登录
     *
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR, "参数为空");

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);

        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取当前登录用户
     *
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);

        LoginUserVO loginUserVO = userService.getLoginUserVO(user);

        return ResultUtils.success(loginUserVO);
    }


    /**
     * 用户退出登录
     *
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "参数为空");

        boolean res = userService.userLogout(request);
        return ResultUtils.success(res);
    }


    /**
     * 创建用户
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<Boolean> userAdd(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR, "参数为空");

        boolean saved = userService.addUser(userAddRequest);
        ThrowUtils.throwIf(!saved, ErrorCode.OPERATION_ERROR, "创建失败");
        return ResultUtils.success(true);
    }

}
