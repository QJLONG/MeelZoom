package com.nemo.mealzoom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nemo.mealzoom.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
