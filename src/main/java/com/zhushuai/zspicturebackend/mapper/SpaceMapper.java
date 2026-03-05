package com.zhushuai.zspicturebackend.mapper;

import com.zhushuai.zspicturebackend.model.entity.Space;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * @author zhushuai
 * @description 针对表【space(空间)】的数据库操作Mapper
 * @createDate 2026-03-05 17:37:20
 * @Entity com.zhushuai.zspicturebackend.model.entity.Space
 */
public interface SpaceMapper extends BaseMapper<Space> {

    /**
     * 获取用户空间统计信息
     *
     * @param userId 用户id
     * @return
     */
    @Select("""
                SELECT
                    IFNULL(SUM(totalSize),0) AS totalSize,
                    IFNULL(SUM(totalCount),0) AS totalPictureCount,
                    count(1) totalCount
                FROM space
                WHERE userId = #{userId}
                  AND isDelete = 0
            """)
    Map<String, Object> getUserSpaceStats(Long userId);

}




