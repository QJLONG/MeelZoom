package com.nemo.mealzoom.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nemo.mealzoom.common.R;
import com.nemo.mealzoom.entity.Employee;
import com.nemo.mealzoom.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @param request
     * @param employee
     * @return
     */
    ;
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
     /* 1、将页面提交的密码password进行md5加密处理
        2、根据页面提交的用户名username查询数据库
        3、如果没有查询到则返回登录失败结果
        4、密码比对，如果不一致则返回登录失败结果
        5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        6、登录成功，将员工id存入Session并返回登录成功结果
        */

        // 1、将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 2.、根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        // 3.、如果没有查询到则返回登录失败结果
        if (emp == null) {
            return R.error("登陆失败");
        }

        // 4.、密码比对，如果不一致则返回登录失败结果
        if (!emp.getPassword().equals(password)) {
            return R.error("登陆失败");
        }

        // 5.、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if (emp.getStatus() == 0) {
            return R.error("账号已禁用");
        }

        // 6.、登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee", emp.getId());

        return R.success(emp);
    }


    /**
     * 员工退出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        // 清理Session中保存的当前登录员工的id
        request.getSession().removeAttribute("employee");

        return R.success("退出成功");
    }

    /**
     * 新增员工
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        Long empId = (Long) request.getSession().getAttribute("employee");
        // 设置默认密码
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        // 设置创建时间
        // employee.setCreateTime(LocalDateTime.now());
        // 设置更新时间
        // employee.setUpdateTime(LocalDateTime.now());
        // 设置创建用户
        // employee.setCreateUser(empId);
        // 设置更新用户
        // employee.setUpdateUser(empId);
        // 保存用户到数据库
        employeeService.save(employee);

        return R.success("新增用户成功");
    }

    /**
     * 实现员工列表的分页查询功能.
     * 注意，在使用分页器之前，需要在配置类中对MybatisPlus的分页器进行配置，详情可见com.nemo.mealzoom.config.MybatisPlusConfig
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        // log.info("page={}, pageSize={}, name={}", page, pageSize, name);
        // 创建分页器
        Page pageInfo = new Page(page, pageSize);
        // 创建条件过滤器
        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 添加过滤条件
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        // 添加排序条件
        lambdaQueryWrapper.orderByDesc(Employee::getUpdateTime);
        // 执行查询
        employeeService.page(pageInfo, lambdaQueryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 更新员工信息，由于前端发送的employeeJson中Status值默认为0(禁用状态），因此在禁用账号时不需要修改employee的status属性。
     * @param request
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee) {
        // log.info("更新员工信息：{}", employee);
        Long empId = (Long) request.getSession().getAttribute("employee");
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(empId);
        // 直接查询是查询不到数据库中的信息的，因为js对Long类型的数据进行处理时会丢失精度（后2位会变成0）
        // 解决方法：在服务端给页面相应数据时，统一将id转为字符串类型。
        // 具体实现：
        //  1.采用SpringMVC框架提供的对象映射器(见com.nemo.common.JacksonObjectMapper)
        //  2.在WebMvcConfig配置类中扩展Spring mvc的消息转换器，在此消息转换器中用上述对象映射进行Java对象到Json字符串的数据转换。
        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }

    /**
     * 编辑用户时，根据id查询用户信息并返回给页面显示。
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id) {
        Employee employee = employeeService.getById(id);
        if (employee != null) {
            return R.success(employee);
        }
        else {
            return R.error("未查询到用户信息");
        }
    }
}
