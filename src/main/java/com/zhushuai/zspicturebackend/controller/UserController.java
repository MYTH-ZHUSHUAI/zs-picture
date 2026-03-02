package com.zhushuai.zspicturebackend.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhushuai.zspicturebackend.annotation.AuthCheck;
import com.zhushuai.zspicturebackend.common.BaseResponse;
import com.zhushuai.zspicturebackend.common.ResultUtils;
import com.zhushuai.zspicturebackend.constant.UserConstant;
import com.zhushuai.zspicturebackend.exception.ErrorCode;
import com.zhushuai.zspicturebackend.exception.ThrowUtils;
import com.zhushuai.zspicturebackend.model.dto.user.UserAddRequest;
import com.zhushuai.zspicturebackend.model.dto.user.UserLoginRequest;
import com.zhushuai.zspicturebackend.model.dto.user.UserQueryRequest;
import com.zhushuai.zspicturebackend.model.dto.user.UserRegisterRequest;
import com.zhushuai.zspicturebackend.model.entity.User;
import com.zhushuai.zspicturebackend.model.vo.LoginUserVO;
import com.zhushuai.zspicturebackend.model.vo.UserVO;
import com.zhushuai.zspicturebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

    /**
     * 根据id获取用户（管理员）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<User> getUserById(Long id) {
        ThrowUtils.throwIf(id < 0, ErrorCode.PARAMS_ERROR, "参数错误");

        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");

        return ResultUtils.success(user);
    }

    /**
     * 根据id获取用户包装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(Long id) {
        BaseResponse<User> baseResponseuserById = this.getUserById(id);
        User user = baseResponseuserById.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 根据id删除用户
     */
    @DeleteMapping("/delete")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<Boolean> deleteUserById(Long id) {
        BaseResponse<User> baseResponseuserById = this.getUserById(id);
        User user = baseResponseuserById.getData();
        boolean b = userService.removeById(user);
        return ResultUtils.success(b);
    }

    /**
     * 分页查询用户
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR, "参数错误");

        long currentPage = userQueryRequest.getCurrentPage();
        long pageSize = userQueryRequest.getPageSize();

        // 执行数据库查询，生成用户分页查询数据
        Page<User> userPage = userService.page(new Page<>(currentPage, pageSize), userService.getQueryWrapper(userQueryRequest));

        // 将分页内的用户数据转换成用户VO数据
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());

        // 新建一个脱敏的用户分页查询数据
        Page<UserVO> userVOPage = new Page<>(currentPage, pageSize, userPage.getTotal());

        userVOPage.setRecords(userVOList);

        return ResultUtils.success(userVOPage);
    }


}
