package com.zhushuai.zspicturebackend.interceptor;


import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.zhushuai.zspicturebackend.model.entity.Space;
import com.zhushuai.zspicturebackend.model.entity.User;
import com.zhushuai.zspicturebackend.model.entity.UserPicture;
import com.zhushuai.zspicturebackend.service.SpaceService;
import com.zhushuai.zspicturebackend.service.SpaceUserService;
import com.zhushuai.zspicturebackend.service.UserPictureService;
import com.zhushuai.zspicturebackend.service.UserService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Component
@Slf4j
public class WsHandshakeInterceptor implements HandshakeInterceptor {


    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private UserPictureService userPictureService;


    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request,
                                   @NonNull ServerHttpResponse response,
                                   @NonNull WebSocketHandler wsHandler,
                                   @NonNull Map<String, Object> attributes) throws Exception {
        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();


        // 获取当前正在编辑的图片参数
        String pictureId = servletRequest.getParameter("pictureId");
        if (StrUtil.isBlank(pictureId)) {
            log.error("pictureId不能为空");
            return false;
        }

        // 获取请求用户
        User loginUser = userService.getLoginUser(servletRequest);
        if (ObjUtil.isEmpty(loginUser)) {
            log.error("用户未登录");
            return false;
        }

        // 获取图片
        UserPicture userPicture = userPictureService.getById(pictureId);
        if (ObjUtil.isEmpty(userPicture)) {
            log.error("图片不存在");
            return false;
        }

        // 获取图片所属空间
        Long spaceId = userPicture.getSpaceId();
        Space space = spaceService.getById(spaceId);
        if (ObjUtil.isEmpty(space) || space.getSpaceType() != 1) {
            log.error("空间不存在或不是团队空间");
            return false;
        }

        // 对用户进行鉴权
        try {
            spaceUserService.getSpaceUserTypeEnum(spaceId, loginUser.getId());
        } catch (Exception e) {
            log.error("用户未加入空间");
            return false;
        }


        attributes.put("pictureId", pictureId);
        attributes.put("spaceId", spaceId);
        attributes.put("loginUser", loginUser);

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               @Nullable Exception exception) {

    }
}
