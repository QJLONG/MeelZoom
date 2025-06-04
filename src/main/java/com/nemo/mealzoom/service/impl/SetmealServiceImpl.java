package com.nemo.mealzoom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nemo.mealzoom.common.CustomException;
import com.nemo.mealzoom.dto.SetmealDto;
import com.nemo.mealzoom.entity.Setmeal;
import com.nemo.mealzoom.entity.SetmealDish;
import com.nemo.mealzoom.mapper.SetmealMapper;
import com.nemo.mealzoom.service.SetmealDishService;
import com.nemo.mealzoom.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;
    /**
     * 新增套餐信息，并保存对应的菜品-套餐关系信息
     * @param setmealDto
     */
    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        // 保存套餐基础信息
        this.save(setmealDto);
        // 为每一个setmealDish添加setMealId
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        List<SetmealDish> list = setmealDishes.stream().map((setMealDish) -> {
            setMealDish.setSetmealId(setmealDto.getId());
            return setMealDish;
        }).collect(Collectors.toList());
        // 保存套餐-菜品关系信息
        setmealDishService.saveBatch(list);
    }

    /**
     * 删除套餐信息，操作setmeal表和setmeal_dish表
     * @param ids
     */
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        // 判断是否存在正在售卖中的套餐
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.in(Setmeal::getId, ids);
        setmealLambdaQueryWrapper.eq(Setmeal::getStatus, 1);
        int count = this.count(setmealLambdaQueryWrapper);
        if (count >= 1) {
            // 如果有，抛出业务异常
            throw new CustomException("套餐正在售卖中，无法删除!");
        }
        // 删除setmeal表中的数据
        this.removeByIds(ids);
        // 删除setmeal_dish 表中的数据
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(setmealDishLambdaQueryWrapper);
    }
}
