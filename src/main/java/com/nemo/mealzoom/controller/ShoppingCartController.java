package com.nemo.mealzoom.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nemo.mealzoom.common.BaseContext;
import com.nemo.mealzoom.common.CustomException;
import com.nemo.mealzoom.common.R;
import com.nemo.mealzoom.entity.ShoppingCart;
import com.nemo.mealzoom.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 增添购物车记录
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        // log.info("shoppingCart: {}", shoppingCart.toString());
        // shoppingCart 对象处理:
        // 设置用户Id，指定是哪个用户的购物车
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);
        // 确定是菜品还是套餐
        Long dishId = shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (dishId != null) {
            // 如果是菜品,查询数据库中是否存在该菜品的购物车信息
            shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId, currentId)
                    .eq(ShoppingCart::getDishId,dishId);
        } else if (setmealId != null){
            // 如果是套餐，与菜品做同样的处理
            shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId, currentId)
                    .eq(ShoppingCart::getSetmealId, setmealId);
        } else {
            // 如果两个id都为空，返回错误信息
            throw new CustomException("请选择菜品或套餐!");
        }
        ShoppingCart oneShoppingCart = shoppingCartService.getOne(shoppingCartLambdaQueryWrapper);
        if (oneShoppingCart != null) {
            // 如果数据库中存在,更新 number + 1
            oneShoppingCart.setNumber(oneShoppingCart.getNumber() + 1);
            shoppingCartService.updateById(oneShoppingCart);
        } else {
            // 如果数据库中不存在，则将 number 设置1，并写入数据库
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            oneShoppingCart = shoppingCart;
        }
        // 返回更新后的shoppingCart
        return R.success(oneShoppingCart);
    }

    /**
     * 购物车记录-1
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {
        // log.info(shoppingCart.toString());
        // 获取当前用户Id
        Long currentId = BaseContext.getCurrentId();
        // 判断传入是套餐还是菜品
        Long dishId = shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        if (dishId != null) {
            queryWrapper.eq(ShoppingCart::getUserId, currentId).eq(ShoppingCart::getDishId, dishId);
        } else {
            queryWrapper.eq(ShoppingCart::getUserId, currentId).eq(ShoppingCart::getSetmealId,setmealId);
        }
        ShoppingCart oneShoppingCart = shoppingCartService.getOne(queryWrapper);
        // 判断数据库中是否存在该数据，获取其number
        if (oneShoppingCart == null) {
            throw new CustomException("购物车不存在该菜品或套餐！");
        }
        Integer number = oneShoppingCart.getNumber();
        // 如果number == 1，直接删除
        if (number == 1) {
            shoppingCartService.removeById(oneShoppingCart.getId());
            oneShoppingCart.setNumber(0);
        } else if (number > 1) {
            // 如果number>1， 更新记录，设置 number -1
            oneShoppingCart.setNumber(number-1);
            shoppingCartService.updateById(oneShoppingCart);
        } else {
            // 如果数据库中查询到的number为0，说明发生了错误！
            throw new CustomException("访问错误！");
        }
        // 返回ShoppingCart
        return R.success(oneShoppingCart);
    }


    /**
     * 显示特定用户的购物车列表
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        // 查询所有此用户的shoppingCart
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);
        // 按照创建时间降序排序
        queryWrapper.orderByDesc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        return R.success(list);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean() {
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);
        shoppingCartService.remove(queryWrapper);
        return R.success("清空成功！");
    }
}
