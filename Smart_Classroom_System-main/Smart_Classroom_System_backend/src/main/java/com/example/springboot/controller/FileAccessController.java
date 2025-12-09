package com.example.springboot.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 图片访问接口（替代静态资源映射，通过流返回图片）
 */
@RestController
@RequestMapping("/file")
public class FileAccessController {

    private static final Logger log = LoggerFactory.getLogger(FileAccessController.class);

    // 从配置文件读取上传根路径（与application.properties一致）
    @Value("${file.upload.path}")
    private String uploadRootPath;

    /**
     * 访问上传的图片（核心接口）
     * 前端访问URL示例：http://localhost:9090/file/access?path=/repair/2025/12/01/xxx.png
     * @param path 图片相对路径（上传接口返回的filePath）
     * @param response 响应对象（用于输出文件流）
     */
    @GetMapping("/access")
    public void accessImage(
            @RequestParam("path") String path,
            HttpServletResponse response) {

        // 1. 参数校验（防止空路径）
        if (path == null || path.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 2. 拼接本地完整文件路径（根路径 + 相对路径）
        // 注意：path开头不要加"/"，如果上传时返回的path带"/"，这里需要处理（避免多一层目录）
        // 修正后代码（添加斜杠拼接）
        String localFilePath = uploadRootPath + File.separator + path.replaceFirst("^/", "");
        File imageFile = new File(localFilePath);

        log.info("访问图片：相对路径={}，本地路径={}", path, localFilePath);

        // 3. 校验文件是否存在
        if (!imageFile.exists() || !imageFile.isFile()) {
            log.error("图片不存在：{}", localFilePath);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            // 4. 设置响应头（关键：告诉浏览器返回的是图片，避免下载）
            String contentType = Files.probeContentType(Paths.get(localFilePath));
            if (contentType == null) {
                // 无法识别类型时，默认按图片处理
                contentType = "image/*";
            }
            response.setContentType(contentType);
            // 支持中文文件名（可选）
            response.setHeader("Content-Disposition",
                    "inline; filename=" + URLEncoder.encode(imageFile.getName(), "UTF-8"));

            // 5. 读取文件流并写入响应
            try (InputStream inputStream = new FileInputStream(imageFile);
                 OutputStream outputStream = response.getOutputStream()) {

                byte[] buffer = new byte[1024 * 8]; // 8KB缓冲区
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
                // 强制刷新输出流
                outputStream.flush();
            }

        } catch (Exception e) {
            log.error("图片访问失败：{}", localFilePath, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}