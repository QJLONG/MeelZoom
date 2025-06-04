package com.nemo.mealzoom.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nemo.mealzoom.common.R;
import com.nemo.mealzoom.dto.DishDto;
import com.nemo.mealzoom.entity.Category;
import com.nemo.mealzoom.entity.Dish;
import com.nemo.mealzoom.service.CategoryService;
import com.nemo.mealzoom.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("新建菜品成功！");
    }

    /**
     * 菜品信息分页查询
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
     * @param dishDto 前端传递过来的dishDto
     * @return 返回描述信息
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        dishService.updateWithFlavor(dishDto);
        return R.success("保存成功！");
    }

    @GetMapping("list")
    public R<List<Dish>> list(Dish dish) {
        // 条件过滤器
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 根据Id查询Dish
        dishLambdaQueryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        // 过滤掉停售的菜品
        dishLambdaQueryWrapper.eq(Dish::getStatus, 1);
        // 排序
        dishLambdaQueryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(dishLambdaQueryWrapper);

        return R.success(list);
    }

}
