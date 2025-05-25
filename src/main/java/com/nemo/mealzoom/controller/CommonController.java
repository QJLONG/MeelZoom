package com.nemo.mealzoom.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nemo.mealzoom.common.CustomException;
import com.nemo.mealzoom.common.R;
import com.nemo.mealzoom.entity.Category;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/common")
public class CommonController {
    // 这里注意，传入的参数名 file 需要与前端表单中输入框的 name 属性一致

    // 获取配置文件中的img目录
    @Value("${mealzoom.image-path}")
    private String imageBasePath;

    /**
     * 上传图片文件
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        File dir = new File(imageBasePath);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new CustomException("图片目录创建失败！");
            }
        }
        // 获取原文件名
        String originalFilename = file.getOriginalFilename();
        // 截取文件后缀
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 用UUID生成新的文件名
        String fileName = UUID.randomUUID().toString() + suffix;
        log.info(imageBasePath);
        try {
            // 将图片转存到本地
            file.transferTo(new File(dir , fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return R.success(fileName);
    }

    /**
     * 从本地下载图片文件并显示浏览器页面中
     * @param name
     * @param response
     * @throws IOException
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) throws IOException {
        // 获取本地图片文件
        File file = new File(imageBasePath, name);
        // 创建输入流
        FileInputStream fileInputStream = null;
        fileInputStream = new FileInputStream(file);
        // 创建输出流
        ServletOutputStream outputStream = response.getOutputStream();
        int len = 0;
        byte[] bytes = new byte[1024];
        while ((len = fileInputStream.read(bytes)) != -1) {
            outputStream.write(bytes, 0, len);
        }
    }


}
