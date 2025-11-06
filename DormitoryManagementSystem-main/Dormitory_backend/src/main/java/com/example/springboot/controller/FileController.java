package com.example.springboot.controller;

import cn.hutool.core.io.FileUtil;
import com.example.springboot.common.Result;
import com.example.springboot.common.UID;
import com.example.springboot.entity.Admin;
import com.example.springboot.entity.DormManager;
import com.example.springboot.entity.Student;
import com.example.springboot.service.AdminService;
import com.example.springboot.service.DormManagerService;
import com.example.springboot.service.StudentService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;

@RestController
@RequestMapping("/files")
public class FileController {

    private static final String ip = "http://localhost";
    static String rootFilePath = System.getProperty("user.dir") + "/springboot/src/main/resources/files/";
    static String originalFilename = "";
    private String port = "9090";

    @Resource
    private StudentService studentService;

    @Resource
    private AdminService adminService;

    @Resource
    private DormManagerService dormManagerService;

    /**
     * 上传文件到本地
     */
    @PostMapping("/upload")
    public Result<?> upload(MultipartFile file) throws IOException {
        originalFilename = file.getOriginalFilename();
        String fileType = originalFilename.substring(originalFilename.lastIndexOf("."), originalFilename.length());
        String uid = new UID().produceUID();
        originalFilename = uid + fileType;
        String targetPath = rootFilePath + originalFilename;
        FileUtil.writeBytes(file.getBytes(), targetPath);
        return Result.success("上传成功");
    }

    /**
     * 更新学生头像
     */
    @PostMapping("/uploadAvatar/stu")
    public Result<?> uploadStuAvatar(@RequestBody Student student) {
        if (originalFilename != null) {
            student.setAvatar(originalFilename);
            int i = studentService.updateNewStudent(student);
            if (i == 1) {
                return Result.success(originalFilename);
            }
        } else {
            return Result.error("-1", "rootFilePath为空");
        }
        return Result.error("-1", "设置头像失败");
    }

    /**
     * 更新管理员头像
     */
    @PostMapping("/uploadAvatar/admin")
    public Result<?> uploadAdminAvatar(@RequestBody Admin admin) {
        if (originalFilename != null) {
            admin.setAvatar(originalFilename);
            int i = adminService.updateAdmin(admin);
            if (i == 1) {
                return Result.success(originalFilename);
            }
        } else {
            return Result.error("-1", "rootFilePath为空");
        }
        return Result.error("-1", "设置头像失败");
    }

    /**
     * 更新宿管头像（核心dormManager相关）
     */
    @PostMapping("/uploadAvatar/dormManager")
    public Result<?> uploadDormManagerAvatar(@RequestBody DormManager dormManager) {
        if (originalFilename != null) {
            dormManager.setAvatar(originalFilename);
            int i = dormManagerService.updateNewDormManager(dormManager);
            if (i == 1) {
                return Result.success(originalFilename);
            }
        } else {
            return Result.error("-1", "rootFilePath为空");
        }
        return Result.error("-1", "设置头像失败");
    }

    /**
     * 加载头像（Base64编码）
     */
    @GetMapping("/initAvatar/{filename}")
    public Result<?> initAvatar(@PathVariable String filename) throws IOException {
        String path = rootFilePath + filename;
        return Result.success(getImage(path));
    }

    private Result<?> getImage(String path) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(path);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int len = -1;
        while ((len = fileInputStream.read(b)) != -1) {
            bos.write(b, 0, len);
        }
        byte[] fileByte = bos.toByteArray();
        Base64.Encoder encoder = Base64.getEncoder();
        String data = encoder.encodeToString(fileByte);
        fileInputStream.close();
        bos.close();
        return Result.success(data);
    }
}