package com.nemo.mealzoom.controller;

import com.nemo.mealzoom.common.R;
import com.nemo.mealzoom.entity.Order;
import com.nemo.mealzoom.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 提交订单（点击“去支付”）
     *
     * @param order
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Order order) {
        orderService.submit(order);
        return R.success("提交成功！");
    }
}
