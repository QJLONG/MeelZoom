package com.nemo.mealzoom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nemo.mealzoom.entity.Employee;
import com.nemo.mealzoom.mapper.EmployeeMapper;
import com.nemo.mealzoom.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
}
