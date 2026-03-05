package com.zhushuai.zspicturebackend.controller;


import com.zhushuai.zspicturebackend.common.BaseResponse;
import com.zhushuai.zspicturebackend.common.ResultUtils;
import com.zhushuai.zspicturebackend.model.dto.space.SpaceAddReq;
import com.zhushuai.zspicturebackend.model.dto.space.SpaceEditReq;
import com.zhushuai.zspicturebackend.model.entity.User;
import com.zhushuai.zspicturebackend.model.vo.SpaceVO;
import com.zhushuai.zspicturebackend.service.SpaceService;
import com.zhushuai.zspicturebackend.service.UserService;
import com.zhushuai.zspicturebackend.service.impl.SpaceServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/space")
public class SpaceController {

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    @Operation(summary = "创建空间")
    @PostMapping("/add")
    public BaseResponse<SpaceVO> spaceAdd(@RequestBody SpaceAddReq spaceAddReq,
                                          HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);


        SpaceVO spaceVO = spaceService.spaceAdd(spaceAddReq, loginUser);

        return ResultUtils.success(spaceVO);
    }


    @Operation(summary = "修改空间")
    @PostMapping("/edit")
    public BaseResponse<SpaceVO> spaceEdit(@RequestBody SpaceEditReq spaceEditReq,
                                           HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);

        SpaceVO spaceVO = spaceService.spaceEdit(spaceEditReq, loginUser);

        return ResultUtils.success(spaceVO);
    }


}
