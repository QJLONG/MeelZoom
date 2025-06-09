package com.nemo.mealzoom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nemo.mealzoom.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
