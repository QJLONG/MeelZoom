package com.nemo.mealzoom.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nemo.mealzoom.common.CustomException;
import com.nemo.mealzoom.common.R;
import com.nemo.mealzoom.dto.DishDto;
import com.nemo.mealzoom.entity.Category;
import com.nemo.mealzoom.entity.Dish;
import com.nemo.mealzoom.entity.DishFlavor;
import com.nemo.mealzoom.entity.SetmealDish;
import com.nemo.mealzoom.service.CategoryService;
import com.nemo.mealzoom.service.DishFlavorService;
import com.nemo.mealzoom.service.DishService;
import com.nemo.mealzoom.service.SetmealDishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.nemo.mealzoom.utils.RedisConstants.DISH_KEY;
import static com.nemo.mealzoom.utils.RedisConstants.DISH_TTL;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 新增菜品
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        // 删除缓存数据
        stringRedisTemplate.delete(DISH_KEY + dishDto.getCategoryId().toString());
        return R.success("新建菜品成功！");
    }

    /**
     * 菜品信息分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        // 分页构造器
        Page<Dish> dishPageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPageInfo = new Page<>(page, pageSize);
        // 条件过滤器
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 查询条件
        dishLambdaQueryWrapper.like(name != null, Dish::getName, name);
        // 排序条件
        dishLambdaQueryWrapper.orderByAsc(Dish::getSort);
        dishService.page(dishPageInfo, dishLambdaQueryWrapper);
        // 由于dishPageInfo中查询到的记录中不包含“菜品名称”，因此需要将记录从Dish转换为DishDto并向其中添加“菜品名称”
        // 将pageInfo信息进行传递（records除外，因为需要另外处理）
        BeanUtils.copyProperties(dishPageInfo, dishDtoPageInfo, "records");
        // 对records的类型进行更换
        List<Dish> records = dishPageInfo.getRecords();
        List<DishDto> newRecords = new ArrayList<>();
        for (Dish item : records) {
            DishDto dishDto = new DishDto();
            // 将dish中除”菜品名称“外的属性拷贝给dishDto
            BeanUtils.copyProperties(item, dishDto);
            // 根据菜品名称id 查询菜品名称
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            // 将对应的菜品分类名称传递给dishDto
            newRecords.add(dishDto);
        }
        // 将新的record传递给dishDtoPageInfo
        dishDtoPageInfo.setRecords(newRecords);
        return R.success(dishDtoPageInfo);
    }

    /**
     * 修改页面获取菜品信息，填充到页面中
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改信息页面中的保存功能
     *
     * @param dishDto 前端传递过来的dishDto
     * @return 返回描述信息
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        dishService.updateWithFlavor(dishDto);
        // 更新后删除该分类在Redis中的缓存
        String queryKey = DISH_KEY + dishDto.getCategoryId().toString();
        stringRedisTemplate.delete(queryKey);
        return R.success("保存成功！");
    }

    // @GetMapping("list")
    // public R<List<Dish>> list(Dish dish) {
    //     // 条件过滤器
    //     LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
    //     // 根据Id查询Dish
    //     dishLambdaQueryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
    //     // 过滤掉停售的菜品
    //     dishLambdaQueryWrapper.eq(Dish::getStatus, 1);
    //     // 排序
    //     dishLambdaQueryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
    //     List<Dish> list = dishService.list(dishLambdaQueryWrapper);
    //
    //     return R.success(list);
    // }

    /**
     * 前端展示菜品信息时，需要向前端传递菜品的口味信息，因此返回DishDto对象
     *
     * @param dish [CategoryId, Status]
     * @return DishDto
     */
    @GetMapping("list")
    public R<List<DishDto>> list(Dish dish) {
        List<DishDto> dishDtoList = new ArrayList<>();
        // 查询Redis，如果有直接返回
        String queryKey = DISH_KEY + dish.getCategoryId().toString();
        String dishDtoListJson = stringRedisTemplate.opsForValue().get(queryKey);
        if (StrUtil.isNotBlank(dishDtoListJson)) {
            dishDtoList = JSONUtil.toList(dishDtoListJson, DishDto.class);
            return R.success(dishDtoList);
        }
        // 如果没有，查询数据库
        // 条件过滤器
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 根据Id查询Dish
        dishLambdaQueryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        // 过滤掉停售的菜品
        dishLambdaQueryWrapper.eq(Dish::getStatus, 1);
        // 排序
        dishLambdaQueryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(dishLambdaQueryWrapper);

        // 为每个Dish对象添加口味信息，生成DishDto对象
        dishDtoList = list.stream().map(item -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId, item.getId());
            List<DishFlavor> flavorList = dishFlavorService.list(dishFlavorLambdaQueryWrapper);
            dishDto.setFlavors(flavorList);
            return dishDto;
        }).collect(Collectors.toList());
        // 将dishDtoList写入缓存
        stringRedisTemplate.opsForValue().set(queryKey, JSONUtil.toJsonStr(dishDtoList), DISH_TTL, TimeUnit.MINUTES);
        return R.success(dishDtoList);
    }


    /**
     * 停售菜品, 停售前检查是否存在关联的正在起售的套餐
     *
     * @param ids
     * @return
     */
    @PostMapping("/status/0")
    public R<String> disable(@RequestParam List<Long> ids) {
        // 检查是否存在关联且正在起售的套餐
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.in(SetmealDish::getDishId, ids)
                .inSql(SetmealDish::getSetmealId, "SELECT id FROM setmeal WHERE status = 0");
        int count = setmealDishService.count(setmealDishLambdaQueryWrapper);
        if (count > 0) {
            throw new CustomException("关联了正在起售的套餐");
        }
        List<Dish> dishes = dishService.listByIds(ids);
        dishes.forEach(dish -> dish.setStatus(0));
        dishService.updateBatchById(dishes);
        // 删除缓存数据
        List<String> queryKeys = ids.stream().map((dishId) -> {
            return DISH_KEY + dishService.getById(dishId).getCategoryId().toString();
        }).collect(Collectors.toList());
        stringRedisTemplate.delete(queryKeys);
        return R.success("停售成功！");
    }

    /**
     * 启售菜品
     *
     * @param ids
     * @return
     */
    @PostMapping("/status/1")
    public R<String> enable(@RequestParam List<Long> ids) {
        List<Dish> dishes = dishService.listByIds(ids);
        dishes.forEach(dish -> dish.setStatus(1));
        dishService.updateBatchById(dishes);
        // 删除缓存数据
        List<String> queryKeys = ids.stream().map((dishId) -> {
            return DISH_KEY + dishService.getById(dishId).getCategoryId().toString();
        }).collect(Collectors.toList());
        stringRedisTemplate.delete(queryKeys);
        return R.success("启售成功！");
    }

    /**
     * 删除菜品
     *
     * @param ids
     * @return
     */
    @DeleteMapping()
    public R<String> delete(@RequestParam List<Long> ids) {
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.in(SetmealDish::getDishId, ids);
        int count = setmealDishService.count(setmealDishLambdaQueryWrapper);
        if (count > 0) {
            throw new CustomException("关联了正在起售的套餐");
        }
        // 删除缓存数据
        List<String> queryKeys = ids.stream().map((dishId) -> {
            return DISH_KEY + dishService.getById(dishId).getCategoryId().toString();
        }).collect(Collectors.toList());
        stringRedisTemplate.delete(queryKeys);
        // 删除数据库中的数据
        dishService.removeByIds(ids);
        return R.success("删除菜品成功！");
    }

}
