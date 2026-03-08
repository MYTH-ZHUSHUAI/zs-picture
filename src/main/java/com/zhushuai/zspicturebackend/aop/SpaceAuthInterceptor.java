package com.zhushuai.zspicturebackend.aop;

import com.zhushuai.zspicturebackend.annotation.SpaceAuthCheck;
import com.zhushuai.zspicturebackend.exception.BusinessException;
import com.zhushuai.zspicturebackend.exception.ErrorCode;
import com.zhushuai.zspicturebackend.exception.ThrowUtils;
import com.zhushuai.zspicturebackend.model.entity.User;
import com.zhushuai.zspicturebackend.model.enums.SpaceUserTypeEnum;
import com.zhushuai.zspicturebackend.service.SpaceUserService;
import com.zhushuai.zspicturebackend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * @author zhushuai
 * 空间权限拦截器
 */
@Aspect
@Component
public class SpaceAuthInterceptor {

    @Resource
    private UserService userService;

    @Resource
    private SpaceUserService spaceUserService;

    /**
     * 进行权限校验
     */
    @Around("@annotation(spaceAuthCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint,
                                SpaceAuthCheck spaceAuthCheck) throws Throwable {

        // 要求的角色
        SpaceUserTypeEnum requiredSpaceUserTypeEnum = spaceAuthCheck.mustRole();

        // 获取请求
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        // 根据请求获取当前用户
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "未找到当前用户");

        // 解析注解参数，从方法参数中获取 spaceId
        Long spaceId = extractSpaceId(joinPoint, request);
        ThrowUtils.throwIf(spaceId == null, ErrorCode.PARAMS_ERROR, "空间Id不能为空");


        // 获取该用户枚举类
        SpaceUserTypeEnum spaceUserTypeEnum = spaceUserService.getSpaceUserTypeEnum(spaceId, loginUser.getId());
        ThrowUtils.throwIf(spaceUserTypeEnum == null, ErrorCode.PARAMS_ERROR, "未找到该用户");

        if (spaceAuthCheck.exactMatch()) {
            // 需要精确匹配角色
            ThrowUtils.throwIf(!spaceUserTypeEnum.equals(requiredSpaceUserTypeEnum), ErrorCode.NO_AUTH_ERROR, "无权限");
        } else {
            // 不需要精确匹配角色
            boolean haveAuth = spaceUserTypeEnum.getValue() >= requiredSpaceUserTypeEnum.getValue();
            ThrowUtils.throwIf(!haveAuth, ErrorCode.NO_AUTH_ERROR, "无权限");
        }


        return joinPoint.proceed();
    }


    /**
     * 从方法参数中提取 spaceId
     */
    private Long extractSpaceId(ProceedingJoinPoint joinPoint, HttpServletRequest request) {


        // 尝试从请求参数获取
        String spaceIdStr = request.getParameter("spaceId");
        if (spaceIdStr != null && !spaceIdStr.isEmpty()) {
            return Long.parseLong(spaceIdStr);
        }

        // 尝试从请求体对象获取（通过方法签名）
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg != null) {
                // 检查对象是否有 getSpaceId 方法
                try {
                    Method method = arg.getClass().getMethod("getSpaceId");
                    Object result = method.invoke(arg);
                    if (result instanceof Long) {
                        return (Long) result;
                    }
                } catch (Exception e) {
                    // 忽略，继续尝试其他方式
                }
            }
        }

        return null;
    }

}
