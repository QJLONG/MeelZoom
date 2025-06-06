package com.nemo.mealzoom.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nemo.mealzoom.common.CustomException;
import com.nemo.mealzoom.common.R;
import com.nemo.mealzoom.dto.SetmealDto;
import com.nemo.mealzoom.entity.Dish;
import com.nemo.mealzoom.entity.Setmeal;
import com.nemo.mealzoom.entity.SetmealDish;
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
     *
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
     *
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
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> remove(@RequestParam List<Long> ids) {
        // log.info("ids: {}", ids);
        setmealService.removeWithDish(ids);
        return R.success("删除成功!");
    }

    /**
     * 填充修改信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id) {
        log.info("id:{}", id);
        SetmealDto setmealDto = setmealService.getByIdWithDish(id);
        return R.success(setmealDto);
    }

    /**
     * 保存修改信息
     *
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto) {
        setmealService.updateWithDish(setmealDto);
        return R.success("保存成功！");
    }

    /**
     * 套餐停售
     *
     * @param ids
     * @return
     */
    @PostMapping("/status/0")
    public R<String> disable(@RequestParam List<Long> ids) {
        List<Setmeal> setmeals = setmealService.listByIds(ids);
        setmeals.forEach(setmeal -> setmeal.setStatus(0));
        setmealService.updateBatchById(setmeals);
        return R.success("停售成功！");
    }

    /**
     * 套餐起售
     *
     * @param ids
     * @return
     */
    @PostMapping("/status/1")
    public R<String> enable(@RequestParam List<Long> ids) {
        // 起售前检查每个套餐中的菜品是否为起售状态
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.in(SetmealDish::getSetmealId, ids)
                .inSql(SetmealDish::getDishId, "SELECT id FROM dish WHERE status=0");
        int count = setmealDishService.count(setmealDishLambdaQueryWrapper);
        if (count > 0) {
            throw new CustomException("套餐中存在停售的菜品！");
        }

        List<Setmeal> setmeals = setmealService.listByIds(ids);
        setmeals.forEach(setmeal -> setmeal.setStatus(1));
        setmealService.updateBatchById(setmeals);
        return R.success("启售成功！");
    }

    /**
     * 根据分类信息获取套餐列表
     *
     * @param setmeal [CategoryId, Status]
     * @return 套餐列表
     */
    @GetMapping("list")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        // log.info(setmeal.toString());
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 根据分类id查询套餐
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, setmeal.getCategoryId());
        // 根据过滤掉Status=0的
        setmealLambdaQueryWrapper.eq(Setmeal::getStatus, setmeal.getStatus());
        List<Setmeal> list = setmealService.list(setmealLambdaQueryWrapper);

        return R.success(list);
    }

    /**
     * 展示套餐详细信息
     * @param setmealId
     * @return
     */
    @GetMapping("/dish/{setmealId}")
    public R<SetmealDto> dish(@PathVariable Long setmealId) {
        // log.info("categoryId: {}", categoryId);
        SetmealDto setmealDto = setmealService.getByIdWithDish(setmealId);
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.eq(SetmealDish::getSetmealId, setmealId);
        setmealDishLambdaQueryWrapper.orderByDesc(SetmealDish::getUpdateTime);
        List<SetmealDish> setmealDishList = setmealDishService.list(setmealDishLambdaQueryWrapper);
        return R.success(setmealDto);
    }
}
