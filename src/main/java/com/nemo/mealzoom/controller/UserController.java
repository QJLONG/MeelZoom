package com.nemo.mealzoom.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nemo.mealzoom.common.R;
import com.nemo.mealzoom.entity.User;
import com.nemo.mealzoom.service.UserService;
import com.nemo.mealzoom.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.nemo.mealzoom.utils.RedisConstants.LOGIN_CODE_KEY;
import static com.nemo.mealzoom.utils.RedisConstants.LOGIN_CODE_TTL;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
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
        // request.getSession().setAttribute(phone, code);
        // 将生成的验证码缓存到redis中，设置有效期为5分钟。
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES); // 5分钟
        return R.success("验证码发送成功！");
    }

    @PostMapping("/login")
    public R<User> login (@RequestBody Map map, HttpServletRequest request) {
        // log.info(map.toString());
        Object phone = map.get("phone");
        Object code = map.get("code");
        // Object sessionCode = request.getSession().getAttribute((String) phone);
        // 从 Redis 中获取验证码
        String sessionCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + (String) phone);
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

        // 如果用户登录成功，删除缓存中的验证码
        stringRedisTemplate.delete(LOGIN_CODE_KEY + (String) phone);
        return R.success(user);
    }
}
