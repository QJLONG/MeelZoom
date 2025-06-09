package com.nemo.mealzoom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nemo.mealzoom.common.BaseContext;
import com.nemo.mealzoom.common.CustomException;
import com.nemo.mealzoom.entity.*;
import com.nemo.mealzoom.mapper.OrderMapper;
import com.nemo.mealzoom.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 提交订单（点击”去支付“）
     *
     * @param order
     */
    @Override
    @Transactional
    public void submit(Order order) {
        // 获取用户信息
        Long currentId = BaseContext.getCurrentId();
        User user = userService.getById(currentId);
        // 获取地址簿信息
        AddressBook addressBook = addressBookService.getById(order.getAddressBookId());
        // 获取购物车信息
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(queryWrapper);
        // 创建atomicInteger对象存储金额，（原子操作，保证数据一致性）
        AtomicInteger amount = new AtomicInteger(0);
        // 填充 order 属性
        order.setId(IdWorker.getId());
        order.setNumber(String.valueOf(IdWorker.getId()));
        order.setStatus(2); // 待派送
        order.setUserId(currentId);
        order.setOrderTime(LocalDateTime.now());
        order.setCheckoutTime(LocalDateTime.now());
        order.setUserName(user.getName());
        order.setPhone(user.getPhone());
        if (addressBook == null){
            throw new CustomException("地址信息为空！");
        }
        order.setAddress((addressBook.getProvinceName()==null ? "" : addressBook.getProvinceName()) +
                (addressBook.getCityName()==null ? "" : addressBook.getCityName()) +
                (addressBook.getDistrictName()==null ? "" : addressBook.getDistrictName()) +
                (addressBook.getDetail()==null ? "" : addressBook.getDetail()));
        order.setConsignee(addressBook.getConsignee());
        // 遍历购物车信息
        if (shoppingCartList == null || shoppingCartList.isEmpty()) {
            throw new CustomException("购物车为空！！");
        }
        List<OrderDetail> orderDetailList = shoppingCartList.stream().map((shoppingCart -> {
            // 创建订单明细并填充信息
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setName(shoppingCart.getName());
            // 这里的orderId在填充order信息时手动填充的
            orderDetail.setOrderId(order.getId());
            orderDetail.setDishId(shoppingCart.getDishId());
            orderDetail.setSetmealId(shoppingCart.getSetmealId());
            orderDetail.setDishFlavor(shoppingCart.getDishFlavor());
            orderDetail.setNumber(shoppingCart.getNumber());
            orderDetail.setAmount(shoppingCart.getAmount());
            orderDetail.setImage(shoppingCart.getImage());
            // 计算订单总价（累加）
            // 用BigDecimal和multiply配合保证高精度
            amount.addAndGet(shoppingCart.getAmount().multiply(new BigDecimal(shoppingCart.getNumber())).intValue());
            return orderDetail;
        })).collect(Collectors.toList());
        // 填充订单总额，保存订单
        order.setAmount(new BigDecimal(amount.get()));
        // order 是mysql数据库保留关键字，如果将其用作数据库表明或列明，需要实体类中重新设置其名字（用反引号将其包围）
        // 这样在生成查询语句时，order 字段会被反引号包围，就不会报错了
        this.save(order);
        // 保存订单明细列表
        orderDetailService.saveBatch(orderDetailList);
        // 清除购物车
        shoppingCartService.remove(queryWrapper);
    }
}
