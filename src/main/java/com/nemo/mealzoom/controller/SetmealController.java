package com.nemo.mealzoom.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nemo.mealzoom.common.R;
import com.nemo.mealzoom.dto.SetmealDto;
import com.nemo.mealzoom.entity.Setmeal;
import com.nemo.mealzoom.service.CategoryService;
import com.nemo.mealzoom.service.SetmealDishService;
import com.nemo.mealzoom.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        setmealService.saveWithDish(setmealDto);
        return R.success("保存成功!");
    }

    /**
     * 分页查询套餐信息，需要对查询到的记录重新构造（添加套餐分类）
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<SetmealDto>> page(int page, int pageSize, String name) {
        // log.info("page: {}, pageSize: {}, name: {}", page, pageSize, name);
        // 分页管理器
        Page<Setmeal> setmealPageInfo = new Page<>();
        Page<SetmealDto> setmealDtoPageInfo = new Page<>();
        // 根据name查询Setmeal记录
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.like(name != null, Setmeal::getName, name);
        setmealLambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(setmealPageInfo, setmealLambdaQueryWrapper);
        // 将SetmealPageInfo的基础信息映射给setmealDtoPageInfo, records 除外，需要添加 categoryName 字段
        BeanUtils.copyProperties(setmealPageInfo, setmealDtoPageInfo, "records");
        // 对setmealPageInfo的records进行处理
        List<Setmeal> records = setmealPageInfo.getRecords();
        List<SetmealDto> newRecords = records.stream().map(record -> {
            // 创建新的SetmealDto，并将setmeal的基础信息拷贝给它
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(record, setmealDto);
            // 获取套餐分类Id
            Long categoryId = record.getCategoryId();
            // 根据CategoryService查询分类名
            String categoryName = categoryService.getById(categoryId).getName();
            // 为新建的SetmealDto设置categoryName
            setmealDto.setCategoryName(categoryName);
            return setmealDto;
        }).collect(Collectors.toList());
        // 为SetmealDtoPageInfo设置处理好的records
        setmealDtoPageInfo.setRecords(newRecords);
        return R.success(setmealDtoPageInfo);
    }

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> remove(@RequestParam List<Long> ids) {
        // log.info("ids: {}", ids);
        setmealService.removeWithDish(ids);
        return R.success("删除成功!");
    }
}
