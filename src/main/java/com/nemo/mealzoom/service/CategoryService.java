package com.nemo.mealzoom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nemo.mealzoom.entity.Category;

public interface CategoryService extends IService<Category> {
     void remove(Long id);
}
