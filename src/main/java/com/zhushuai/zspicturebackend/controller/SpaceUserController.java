package com.zhushuai.zspicturebackend.controller;


import com.zhushuai.zspicturebackend.common.BaseResponse;
import com.zhushuai.zspicturebackend.common.ResultUtils;
import com.zhushuai.zspicturebackend.model.dto.spaceuser.SpaceUserAddReq;
import com.zhushuai.zspicturebackend.model.entity.User;
import com.zhushuai.zspicturebackend.model.vo.SpaceUserVO;
import com.zhushuai.zspicturebackend.service.SpaceUserService;
import com.zhushuai.zspicturebackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/spaceUser")
public class SpaceUserController {

    @Resource
    private UserService userService;

    @Resource
    private SpaceUserService spaceUserService;

    @PostMapping("/add")
    @Operation(summary = "添加空间用户")
    public BaseResponse<SpaceUserVO> spaceUserAdd(@RequestBody SpaceUserAddReq spaceUserAddReq,
                                                  HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);

        return ResultUtils.success(spaceUserService.addSpaceUser(spaceUserAddReq));
    }
}
