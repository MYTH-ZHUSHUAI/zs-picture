package com.zhushuai.zspicturebackend.constant;

/**
 * @author zhushuai
 * Redis 常量类
 */
public interface RedisConstant {

    /**
     * 用户登录 Redis Key 前缀
     */
    String USER_LOGIN_KEY_PREFIX = "user:login:";

    /**
     * 用户登录 Redis Key 模板
     * @param userId 用户 ID
     * @return Redis Key
     */
    static String getUserLoginKey(Long userId) {
        return USER_LOGIN_KEY_PREFIX + userId;
    }

    /**
     * Session 过期时间（30 天）
     */
    long SESSION_EXPIRE_TIME = 2592000L;
}
