package com.nemo.mealzoom.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nemo.mealzoom.common.R;
import com.nemo.mealzoom.entity.User;
import com.nemo.mealzoom.service.UserService;
import com.nemo.mealzoom.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    /**
     * 发送短信验证码
     * @param user 用于接收手机号
     * @param request
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sentMsg (@RequestBody User user, HttpServletRequest request) {
        String phone = user.getPhone();
        String code = ValidateCodeUtils.generateValidateCode(4).toString();
        // 模拟发送短信
        log.info("验证码：{}", code);
        request.getSession().setAttribute(phone, code);
        return R.success("验证码发送成功！");
    }

    @PostMapping("/login")
    public R<User> login (@RequestBody Map map, HttpServletRequest request) {
        // log.info(map.toString());
        Object phone = map.get("phone");
        Object code = map.get("code");
        Object sessionCode = request.getSession().getAttribute((String) phone);
        if (!code.equals(sessionCode)) {
            // 验证失败，返回错误信息
            return R.error("验证码错误！");
        }
        // 验证成功，检查用户表中是否存在该用户
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getPhone, phone);
        User user = userService.getOne(userLambdaQueryWrapper);
        if (user == null) {
            // 用户不存在，创建新用户
            user = new User();
            user.setPhone((String) phone);
            user.setStatus(1);
            userService.save(user);
        }
        // 向Session写入用户id
        request.getSession().setAttribute("user", user.getId());
        return R.success(user);
    }
}
