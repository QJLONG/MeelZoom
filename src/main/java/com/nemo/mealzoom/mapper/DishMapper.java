package com.nemo.mealzoom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nemo.mealzoom.entity.Dish;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}
