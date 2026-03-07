package com.zhushuai.zspicturebackend.controller;


import com.zhushuai.zspicturebackend.common.BaseResponse;
import com.zhushuai.zspicturebackend.common.ResultUtils;
import com.zhushuai.zspicturebackend.model.dto.spaceuser.SpaceUserAddReq;
import com.zhushuai.zspicturebackend.model.dto.spaceuser.SpaceUserDeleteReq;
import com.zhushuai.zspicturebackend.model.dto.spaceuser.SpaceUserEditReq;
import com.zhushuai.zspicturebackend.model.dto.spaceuser.SpaceUserQueryReq;
import com.zhushuai.zspicturebackend.model.entity.User;
import com.zhushuai.zspicturebackend.model.vo.SpaceUserVO;
import com.zhushuai.zspicturebackend.model.vo.UserVO;
import com.zhushuai.zspicturebackend.service.SpaceUserService;
import com.zhushuai.zspicturebackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

    /**
     * 获取空间用户列表
     *
     * @param SpaceUserQueryReq
     * @param request
     * @return
     */
    @PostMapping("/listuser")
    @Operation(summary = "获取空间用户列表")
    public BaseResponse<List<UserVO>> spaceUserList(@RequestBody SpaceUserQueryReq SpaceUserQueryReq,
                                                    HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);

        return ResultUtils.success(spaceUserService.getSpaceUserList(SpaceUserQueryReq.getSpaceId()));
    }


    /**
     * 删除空间用户
     *
     * @param spaceUserDeleteReq
     * @param request
     * @return
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除空间用户")
    public BaseResponse<Boolean> spaceUserDelete(@RequestBody SpaceUserDeleteReq spaceUserDeleteReq,
                                                 HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);

        return ResultUtils.success(spaceUserService.deleteSpaceUser(spaceUserDeleteReq));
    }


    /**
     * 编辑空间用户
     */
    @PostMapping("/edit")
    @Operation(summary = "编辑空间用户")
    public BaseResponse<SpaceUserVO> spaceUserEdit(@RequestBody SpaceUserEditReq spaceUserEditReq,
                                                   HttpServletRequest request) {

        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(spaceUserService.editSpaceUser(spaceUserEditReq));
    }
}
