package com.nemo.mealzoom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nemo.mealzoom.dto.SetmealDto;
import com.nemo.mealzoom.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
     public void saveWithDish (SetmealDto setmealDto);

     public void removeWithDish (List<Long> ids);
}
