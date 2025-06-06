package com.nemo.mealzoom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nemo.mealzoom.dto.SetmealDto;
import com.nemo.mealzoom.entity.Setmeal;
import com.nemo.mealzoom.entity.SetmealDish;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
     public void saveWithDish (SetmealDto setmealDto);

     public void removeWithDish (List<Long> ids);

     public SetmealDto getByIdWithDish (Long id);

     public void updateWithDish (SetmealDto setmealDto);
}
