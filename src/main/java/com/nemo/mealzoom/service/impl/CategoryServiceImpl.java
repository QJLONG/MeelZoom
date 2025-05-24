package com.nemo.mealzoom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nemo.mealzoom.common.CustomException;
import com.nemo.mealzoom.entity.Category;
import com.nemo.mealzoom.entity.Dish;
import com.nemo.mealzoom.entity.Setmeal;
import com.nemo.mealzoom.mapper.CategoryMapper;
import com.nemo.mealzoom.service.CategoryService;
import com.nemo.mealzoom.service.DishService;
import com.nemo.mealzoom.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    DishService dishService;

    @Autowired
    SetmealService setmealService;
    /**
     * 删除分类方法
     *
     * @param id
     */
    @Override
    public void remove(Long id) {
        // 判断该分类是否有关联的菜品
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
        int dishCount = dishService.count(dishLambdaQueryWrapper);
        if (dishCount > 0) {
            // 如果存在关联的菜品，抛出自定义异常，并用全局异常进行捕获，返回异常信息到页面。
            throw new CustomException("存在" + dishCount + "个关联的菜品，删除失败！");
        }
        // 判断该分类是否有关联的套餐
        LambdaQueryWrapper<Setmeal> setmealServiceLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealServiceLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        int setmealCount = setmealService.count(setmealServiceLambdaQueryWrapper);
        if (setmealCount > 0) {
            // 如果存在关联的套餐，抛出自定义异常，并被全局异常捕获
            throw new CustomException("存在" + setmealCount + "个关联的套餐，删除失败！");
        }
        // 删除分类
        super.removeById(id);
    }
}
