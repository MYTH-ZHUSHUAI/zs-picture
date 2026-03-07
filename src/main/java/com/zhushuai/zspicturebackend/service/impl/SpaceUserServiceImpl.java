package com.zhushuai.zspicturebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhushuai.zspicturebackend.model.entity.SpaceUser;
import com.zhushuai.zspicturebackend.service.SpaceUserService;
import com.zhushuai.zspicturebackend.mapper.SpaceUserMapper;
import org.springframework.stereotype.Service;

/**
* @author zhushuai
* @description 针对表【space_user(空间用户关联)】的数据库操作Service实现
* @createDate 2026-03-07 19:54:53
*/
@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
    implements SpaceUserService{

}




