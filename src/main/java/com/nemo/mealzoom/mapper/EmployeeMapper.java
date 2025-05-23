package com.nemo.mealzoom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nemo.mealzoom.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}
