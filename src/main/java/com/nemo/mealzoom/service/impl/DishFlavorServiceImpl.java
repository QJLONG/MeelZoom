package com.nemo.mealzoom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nemo.mealzoom.entity.DishFlavor;
import com.nemo.mealzoom.mapper.DishFlavorMapper;
import com.nemo.mealzoom.service.DishFlavorService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
