package com.zhushuai.zspicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhushuai.zspicturebackend.constant.UserConstant;
import com.zhushuai.zspicturebackend.exception.BusinessException;
import com.zhushuai.zspicturebackend.exception.ErrorCode;
import com.zhushuai.zspicturebackend.exception.ThrowUtils;
import com.zhushuai.zspicturebackend.model.dto.user.UserAddRequest;
import com.zhushuai.zspicturebackend.model.dto.user.UserQueryRequest;
import com.zhushuai.zspicturebackend.model.entity.User;
import com.zhushuai.zspicturebackend.model.enums.UserRoleEnum;
import com.zhushuai.zspicturebackend.model.vo.LoginUserVO;
import com.zhushuai.zspicturebackend.model.vo.UserVO;
import com.zhushuai.zspicturebackend.service.UserService;
import com.zhushuai.zspicturebackend.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhushuai
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2026-02-26 10:17:00
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {

        // 1. 校验参数
        ThrowUtils.throwIf(StringUtils.isAnyBlank(userAccount, userPassword, checkPassword), ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号过短");
        ThrowUtils.throwIf(userPassword.length() < 8 || checkPassword.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码过短");
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        // 2. 用户名是否重复
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount", userAccount);
        long count = this.baseMapper.selectCount(userQueryWrapper);
        ThrowUtils.throwIf(count > 0, ErrorCode.OPERATION_ERROR, "用户已存在");

        // 3. 密码加密
        String encryptedPassword = getEncryptedPassword(userPassword);


        // 4. 插入数据库
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptedPassword);
        user.setUserRole(UserRoleEnum.USER.getValue());
        user.setUserName(userAccount);
        boolean saveRes = this.save(user);
        ThrowUtils.throwIf(!saveRes, ErrorCode.OPERATION_ERROR, "新增用户失败");

        // 主键回填
        return user.getId();


    }

    /**
     * 获取加密密码
     *
     * @param userPassword
     * @return
     */
    @Override
    public String getEncryptedPassword(String userPassword) {
        // 加盐，混淆密码
        final String salt = "zhushuai";

        userPassword = salt + userPassword;

        return DigestUtils.md5DigestAsHex(userPassword.getBytes());
    }

    /**
     * 用户登录
     *
     * @param userAccount
     * @param userPassword
     * @param request
     * @return 脱敏后的用户信息
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {

        // 1. 校验参数
        ThrowUtils.throwIf(StringUtils.isAnyBlank(userAccount, userPassword), ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号错误");
        ThrowUtils.throwIf(userPassword.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码错误");


        // 2. 对用户密码进行加密
        String encryptedPassword = getEncryptedPassword(userPassword);


        // 3. 查询用户是否存在
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount", userAccount);
        userQueryWrapper.eq("userPassword", encryptedPassword);
        User user = this.baseMapper.selectOne(userQueryWrapper);
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");


        // 4. 保存用户的登录状态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);

        return this.getLoginUserVO(user);
    }

    /**
     * 获取当前登录用户vo
     *
     * @param user
     * @return
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }

        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;

    }

    /**
     * 获取脱敏的用户信息列表
     *
     * @param userList
     * @return
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return Collections.emptyList();
        }

        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    /**
     * 通过一次http请求获取当前登录用户
     *
     * @param request
     * @return user实体类
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 判断是否登录
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 从数据库中再查出来
        currentUser = this.baseMapper.selectById(currentUser.getId());
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        return currentUser;
    }

    /**
     * 退出登录
     *
     * @param request
     * @return
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 判断是否登录
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户未登录");
        }

        // 移除登录状态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;

    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userAvatar = userQueryRequest.getUserAvatar();
        String userProfile = userQueryRequest.getUserProfile();
        long currentPage = userQueryRequest.getCurrentPage();
        long pageSize = userQueryRequest.getPageSize();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq(id != null, "id", id);
        userQueryWrapper.eq(StringUtils.isNotBlank(userAccount), "userAccount", userAccount);
        userQueryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        userQueryWrapper.like(StringUtils.isNotBlank(userAvatar), "userAvatar", userAvatar);
        userQueryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        userQueryWrapper.orderBy(StringUtils.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);

        return userQueryWrapper;
    }

    /**
     * 管理员新增用户
     *
     * @param userAddRequest 前端传入的参数
     * @return
     */
    @Override
    public boolean addUser(UserAddRequest userAddRequest) {

        // 将前端发来的请求转为实体类
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        user.setUserPassword(this.getEncryptedPassword(user.getUserAccount() + UserConstant.USER_DEFAULT_PASSWORD));

        return this.save(user);
    }

    /**
     * 判断是否为管理员
     *
     * @param user 用户
     * @return
     */
    @Override
    public boolean isAdmin(User user) {
        if (user == null) {
            return false;
        }
        return user.getUserRole().equals(UserConstant.USER_ROLE_ADMIN);
    }


    /**
     * 根据ids获取用户列表
     *
     * @param userList
     * @return
     */
    @Override
    public List<UserVO> listUserVO(List<Long> userList) {
        if (CollUtil.isEmpty(userList)) {
            return Collections.emptyList();
        }


        List<User> users = this.listByIds(userList);

        // 转换为 UserVO 并返回
        return getUserVOList(users);
    }
}




