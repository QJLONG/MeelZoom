package com.nemo.mealzoom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nemo.mealzoom.entity.Dish;
import com.nemo.mealzoom.mapper.DishMapper;
import com.nemo.mealzoom.service.DishService;
import org.springframework.stereotype.Service;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
}
