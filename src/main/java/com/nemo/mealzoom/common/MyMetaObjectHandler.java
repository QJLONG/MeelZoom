package com.nemo.mealzoom.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.fasterxml.jackson.databind.ser.Serializers;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 自定义元数据对象处理器,用于对公共字段进行统一处理
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    /**
     * 插入操作自动填充，将数据插入数据库之前的操作
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("insertFill...");
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("createTime", LocalDateTime.now());
        // 通过 ThreadLocalu 获取当前用户Id
        metaObject.setValue("updateUser", BaseContext.getCurrentId());
        metaObject.setValue("createUser", BaseContext.getCurrentId());
    }

    /**
     * 跟新操作自动填充。
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("updateFIll...");
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());
    }
}
