package com.nemo.mealzoom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nemo.mealzoom.entity.Order;

public interface OrderService extends IService<Order> {
    public void submit(Order order);
}
