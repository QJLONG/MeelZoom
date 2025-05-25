package com.nemo.mealzoom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nemo.mealzoom.dto.DishDto;
import com.nemo.mealzoom.entity.Dish;


public interface DishService extends IService<Dish> {
    public void saveWithFlavor(DishDto dishDto);
}
