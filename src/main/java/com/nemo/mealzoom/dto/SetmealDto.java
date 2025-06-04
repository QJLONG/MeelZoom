package com.nemo.mealzoom.dto;

import com.nemo.mealzoom.entity.Setmeal;
import com.nemo.mealzoom.entity.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
