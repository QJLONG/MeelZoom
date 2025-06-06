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
import org.springframework.beans.BeanUtils;
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

    /**
     * 填充修改界面信息, 查询 setmeal, setmeal_dish两张表
     * @param id
     */
    @Override
    public SetmealDto getByIdWithDish(Long id) {
        // 获取套餐基本信息并映射给Dto对象
        SetmealDto setmealDto = new SetmealDto();
        Setmeal setmeal = this.getById(id);
        BeanUtils.copyProperties(setmeal, setmealDto);
        // 获取对应的setmeal_dish信息列表
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.eq(SetmealDish::getSetmealId, id).orderByDesc(SetmealDish::getUpdateTime);
        List<SetmealDish> setmealDishList = setmealDishService.list(setmealDishLambdaQueryWrapper);
        // 将setmeal_dish信息列表映射给Dto对象
        setmealDto.setSetmealDishes(setmealDishList);
        return setmealDto;
    }

    /**
     * 保存修改界面信息
     * @param setmealDto
     */
    @Override
    @Transactional
    public void updateWithDish(SetmealDto setmealDto) {
        // 更新基本信息
        this.updateById(setmealDto);
        // 删除原有 setmeal_dish 记录
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(setmealDishLambdaQueryWrapper);
        // 保存信的 setmeal_dish 记录
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        List<SetmealDish> list = setmealDishes.stream().map((setmealDish) -> {
            setmealDish.setSetmealId(setmealDto.getId());
            return setmealDish;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(list);
    }
}
