package com.nemo.mealzoom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nemo.mealzoom.dto.DishDto;
import com.nemo.mealzoom.entity.Dish;
import com.nemo.mealzoom.entity.DishFlavor;
import com.nemo.mealzoom.mapper.DishMapper;
import com.nemo.mealzoom.service.DishFlavorService;
import com.nemo.mealzoom.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional // 由于操作了两张数据表，需要添加Transactional，同时在启动类中添加@EnableTransactionManagement注解
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;
    @Override
    public void saveWithFlavor(DishDto dishDto) {
        // 将dish数据保存到dish表中
        this.save(dishDto);
        // 获取dish的id
        Long dishId = dishDto.getId();
        // 向每个flavor中添加dishId
        List<DishFlavor> flavors = dishDto.getFlavors();
        // for (DishFlavor flavor: flavors) {
        //     flavor.setDishId(dishId);
        // }
        flavors.forEach(flavor -> flavor.setDishId(dishId));
        dishFlavorService.saveBatch(flavors);
    }
}
