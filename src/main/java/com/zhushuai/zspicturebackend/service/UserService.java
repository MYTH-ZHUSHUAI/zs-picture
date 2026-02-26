package com.zhushuai.zspicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhushuai.zspicturebackend.model.dto.user.UserAddRequest;
import com.zhushuai.zspicturebackend.model.dto.user.UserQueryRequest;
import com.zhushuai.zspicturebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhushuai.zspicturebackend.model.vo.LoginUserVO;
import com.zhushuai.zspicturebackend.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author zhushuai
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2026-02-26 10:17:00
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);


    /**
     * 获取加密密码
     *
     * @param userPassword
     * @return
     */
    String getEncryptedPassword(String userPassword);

    /**
     *
     * @param userAccount
     * @param userPassword
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取登录后脱敏的用户信息
     *
     * @param user
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息列表
     *
     */
    List<UserVO> getUserVOList(List<User> userList);


    /**
     * 服务器内获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 退出登录
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);


    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);


    /**
     * 管理员新增用户
     *
     * @param userAddRequest 前端传入的参数
     * @return
     */
    boolean addUser(UserAddRequest userAddRequest);

}
