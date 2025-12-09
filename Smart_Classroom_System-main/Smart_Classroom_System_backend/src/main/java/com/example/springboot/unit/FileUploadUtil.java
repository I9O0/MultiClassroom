package com.example.springboot.unit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Component
public class FileUploadUtil {

    // 配置文件中配置的上传路径
    @Value("${file.upload.path}")
    private String uploadPath;

    // 访问图片的基础URL
    @Value("${file.access.url}")
    private String accessUrl;

    /**
     * 上传文件并返回访问路径
     */
    public String uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        // 获取文件原始名称
        String originalFilename = file.getOriginalFilename();
        // 获取文件后缀
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 生成唯一文件名，避免重复
        String fileName = UUID.randomUUID().toString() + suffix;

        // 创建日期目录，按日期分类存储
        String dateDir = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
        File dateDirFile = new File(uploadPath + "/repair/" + dateDir);
        if (!dateDirFile.exists()) {
            dateDirFile.mkdirs();
        }

        // 保存文件
        File dest = new File(dateDirFile, fileName);
        file.transferTo(dest);

        // 返回相对访问路径
        return "/repair/" + dateDir + "/" + fileName;
    }
}