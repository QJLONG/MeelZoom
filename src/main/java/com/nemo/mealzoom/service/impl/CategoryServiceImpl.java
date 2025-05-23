package com.nemo.mealzoom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nemo.mealzoom.entity.Category;
import com.nemo.mealzoom.mapper.CategoryMapper;
import com.nemo.mealzoom.service.CategoryService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
}
