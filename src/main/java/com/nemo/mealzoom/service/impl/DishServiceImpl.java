package com.nemo.mealzoom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nemo.mealzoom.dto.DishDto;
import com.nemo.mealzoom.entity.Dish;
import com.nemo.mealzoom.entity.DishFlavor;
import com.nemo.mealzoom.mapper.DishMapper;
import com.nemo.mealzoom.service.DishFlavorService;
import com.nemo.mealzoom.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional // 由于操作了两张数据表，需要添加Transactional，同时在启动类中添加@EnableTransactionManagement注解
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 保存菜品信息，同时保存口味信息
     * @param dishDto
     */
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

    /**
     * 获取菜品信息，同时获取口味信息
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        DishDto dishDto = new DishDto();
        Dish dish = this.getById(id);
        // 将菜品基础信息映射给dishDto
        BeanUtils.copyProperties(dish, dishDto);
        // 获取菜品对应的口味信息
        LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId, id);
        List<DishFlavor> list = dishFlavorService.list(dishFlavorLambdaQueryWrapper);
        // 为dishDto设置口味信息
        dishDto.setFlavors(list);
        return dishDto;
    }

    /**
     * 更新菜品信息，同时更新口味信息：
     *  1.更新菜品基础信息
     *  2.删除菜品对应的口味信息
     *  3.重提保存口味信息
     * @param dishDto
     */
    @Override
    public void updateWithFlavor(DishDto dishDto) {
        // 更新菜品基本信息
        this.updateById(dishDto);
        // 删除原有的口味信息
        LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(dishFlavorLambdaQueryWrapper);
        // 添加新的口味信息
        List<DishFlavor> flavors = dishDto.getFlavors();
        // 新添加的口味信息flavor中没有dishid，需要手动添加
        flavors.forEach((flavor)->flavor.setDishId(dishDto.getId()));
        dishFlavorService.saveBatch(flavors);
    }
}
