package com.zhushuai.zspicturebackend.annotation;

import com.zhushuai.zspicturebackend.model.enums.SpaceUserTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author zhushuai
 * 空间权限检查注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SpaceAuthCheck {

    /**
     * 必须有该空间角色或更高级别的角色
     * 可选值：viewer, editor, admin
     * 权限级别：viewer < editor < admin
     */
    SpaceUserTypeEnum mustRole() default SpaceUserTypeEnum.VIEWER;

    /**
     * 是否需要精确匹配角色，不匹配则拒绝访问
     * 默认 false，表示可以有更高权限
     */
    boolean exactMatch() default false;
}
