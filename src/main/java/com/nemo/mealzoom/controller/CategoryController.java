package com.nemo.mealzoom.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nemo.mealzoom.common.R;
import com.nemo.mealzoom.entity.Category;
import com.nemo.mealzoom.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 添加分类
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category) {
        categoryService.save(category);
        return R.success("添加分类成功！");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize) {
        // 创建分页构造器
        Page<Category> pageInfo = new Page<>(page, pageSize);
        // 创建条件过滤器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        // 设置升序排序
        queryWrapper.orderByAsc(Category::getSort);
        categoryService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 删除分类：自定义删除分类方法
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long ids) {
        Long id = ids;
        log.info("删除分类，id为：{}", id);
        categoryService.remove(id);
        return R.success("删除分类成功!");
    }

    @PutMapping
    public R<String> update(@RequestBody Category category) {
        log.info("修改分类信息：{}", category);
        categoryService.updateById(category);
        return R.success("修改分类成功");
    }
}
