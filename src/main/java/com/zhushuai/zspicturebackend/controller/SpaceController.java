package com.zhushuai.zspicturebackend.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhushuai.zspicturebackend.annotation.AuthCheck;
import com.zhushuai.zspicturebackend.common.BaseResponse;
import com.zhushuai.zspicturebackend.common.ResultUtils;
import com.zhushuai.zspicturebackend.model.dto.space.SpaceAddReq;
import com.zhushuai.zspicturebackend.model.dto.space.SpaceEditReq;
import com.zhushuai.zspicturebackend.model.dto.space.SpaceQueryReq;
import com.zhushuai.zspicturebackend.model.entity.Space;
import com.zhushuai.zspicturebackend.model.entity.User;
import com.zhushuai.zspicturebackend.model.vo.SpaceVO;
import com.zhushuai.zspicturebackend.service.SpaceService;
import com.zhushuai.zspicturebackend.service.UserService;
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


    /**
     * 修改空间
     *
     * @param spaceEditReq
     * @param request
     * @return
     */
    @Operation(summary = "修改空间")
    @PostMapping("/edit")
    public BaseResponse<SpaceVO> spaceList(@RequestBody SpaceEditReq spaceEditReq,
                                           HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);

        SpaceVO spaceVO = spaceService.spaceEdit(spaceEditReq, loginUser);

        return ResultUtils.success(spaceVO);
    }


    /**
     * 管理员获取对应用户的所有空间
     *
     * @param spaceQueryReq
     * @param request
     * @return
     */
    @Operation(summary = "管理员分页查询空间")
    @PostMapping("/listadmin")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Page<Space>> spaceList(@RequestBody SpaceQueryReq spaceQueryReq,
                                               HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);

        Page<Space> spaceList = spaceService.getSpaceList(spaceQueryReq, loginUser);

        return ResultUtils.success(spaceList);
    }


    /**
     * 获取对应用户的所有空间
     *
     * @param spaceQueryReq
     * @param request
     * @return
     */
    @Operation(summary = "普通用户分页查询空间")
    @PostMapping("/listuser")
    public BaseResponse<Page<SpaceVO>> spaceVOList(@RequestBody SpaceQueryReq spaceQueryReq,
                                                   HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);

        Page<SpaceVO> spaceList = spaceService.getSpaceVOList(spaceQueryReq, loginUser);

        return ResultUtils.success(spaceList);
    }


    /**
     * 删除空间
     *
     * @param space
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @Operation(summary = "删除空间")
    public BaseResponse<Integer> deleteSpace(Long id,
                                             HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);

        int delete = spaceService.deleteSpace(id);

        return ResultUtils.success(delete);
    }


}
