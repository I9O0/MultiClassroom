//package com.example.springboot.controller;
//
//import cn.hutool.core.io.FileUtil;
//import com.example.springboot.common.Result;
//import com.example.springboot.common.UID;
//import com.example.springboot.entity.Admin;
//import com.example.springboot.entity.Manager;  // 修正类名大写规范
//import com.example.springboot.entity.Student;
//import com.example.springboot.service.AdminService;
//import com.example.springboot.service.ManagerService;  // 修正类名大写规范
//import com.example.springboot.service.StudentService;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import javax.annotation.PostConstruct;
//import javax.annotation.Resource;
//import javax.servlet.http.HttpSession;
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.Base64;
//import java.util.List;
//
//@RestController
//@RequestMapping("/files")
//public class FileController {
//
//    private static final String ip = "http://localhost";
//    // 从配置文件注入路径（替换原来的硬编码）
//    @Value("${file.upload.path}")
//    private String rootFilePath;
//
//    static String originalFilename = "";
//    private String port = "9090";
//
//    @Resource
//    private StudentService studentService;
//
//    @Resource
//    private AdminService adminService;
//
//    @Resource
//    private ManagerService managerService;  // 修正类名大写规范
//
//    // 初始化：检查并创建上传目录
//    @PostConstruct
//    public void init() {
//        File dir = new File(rootFilePath);
//        if (!dir.exists()) {
//            dir.mkdirs();  // 递归创建目录
//        }
//    }
//
//    /**
//     * 上传文件到本地（限制图片类型和大小）
//     */
//    @PostMapping("/upload")
//    public Result<?> upload(MultipartFile file) throws IOException {
//        originalFilename = file.getOriginalFilename();
//        String fileType = originalFilename.substring(originalFilename.lastIndexOf("."), originalFilename.length());
//        String uid = new UID().produceUID();
//        originalFilename = uid + fileType;
//        String targetPath = rootFilePath + originalFilename;
//
//        // 自动创建目录（如果不存在）
//        File targetFile = new File(targetPath);
//        if (!targetFile.getParentFile().exists()) {
//            targetFile.getParentFile().mkdirs(); // 递归创建所有父目录
//        }
//
//        FileUtil.writeBytes(file.getBytes(), targetPath);
//        return Result.success("上传成功");
//    }
//
//    /**
//     * 更新学生头像（带权限校验）
//     */
//    @PostMapping("/uploadAvatar/stu")
//    public Result<?> uploadStuAvatar(@RequestBody Student student,
//                                     @RequestParam String filename,  // 接收上传时返回的文件名
//                                     HttpSession session) {
//        // 权限校验：只能修改当前登录用户的头像
//        String loginUsername = (String) session.getAttribute("username");
//        if (loginUsername == null || !loginUsername.equals(student.getUsername())) {
//            return Result.error("-1", "无权修改他人头像");
//        }
//
//        student.setAvatar(filename);
//        int i = studentService.updateNewStudent(student);
//        return i == 1 ? Result.success(filename) : Result.error("-1", "设置头像失败");
//    }
//
//    /**
//     * 更新管理员头像（带权限校验）
//     */
//    /**
//     * 更新管理员头像（修正版：通过 Admin 对象的 avatar 字段接收文件名）
//     */
//    @PostMapping("/uploadAvatar/admin")
//    public Result<?> uploadAdminAvatar(@RequestBody Admin admin) {
//        // 从 Admin 对象中获取前端传递的文件名（即上传后的头像文件名）
//        String filename = admin.getAvatar();
//        if (filename != null && !filename.isEmpty()) {
//            // 直接使用传递的文件名更新头像
//            int i = adminService.updateAdmin(admin);
//            if (i == 1) {
//                return Result.success(filename);
//            }
//        } else {
//            return Result.error("-1", "头像文件名不能为空");
//        }
//        return Result.error("-1", "设置头像失败");
//    }
//
//    /**
//     * 更新宿管头像（带权限校验）
//     */
//    @PostMapping("/uploadAvatar/manager")
//    public Result<?> uploadManagerAvatar(@RequestBody Manager manager,  // 修正类名大写规范
//                                         @RequestParam String filename,
//                                         HttpSession session) {
//        String loginUsername = (String) session.getAttribute("username");
//        if (loginUsername == null || !loginUsername.equals(manager.getUsername())) {
//            return Result.error("-1", "无权修改他人头像");
//        }
//
//        manager.setAvatar(filename);
//        int i = managerService.updateNewmanager(manager);  // 修正方法名大写规范
//        return i == 1 ? Result.success(filename) : Result.error("-1", "设置头像失败");
//    }
//
//    /**
//     * 加载头像（Base64编码，自动关闭资源）
//     */
//    @GetMapping("/initAvatar/{filename}")
//    public Result<?> initAvatar(@PathVariable String filename) throws IOException {
//        String path = rootFilePath + filename;
//        // 校验文件是否存在
//        if (!FileUtil.exist(path)) {
//            return Result.error("-1", "头像文件不存在");
//        }
//        return Result.success(getImage(path));
//    }
//
//    // 工具方法：将图片转为Base64（使用try-with-resources自动关闭流）
//    private String getImage(String path) throws IOException {
//        try (FileInputStream fis = new FileInputStream(path);
//             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
//            byte[] buffer = new byte[1024];
//            int len;
//            while ((len = fis.read(buffer)) != -1) {
//                bos.write(buffer, 0, len);
//            }
//            return Base64.getEncoder().encodeToString(bos.toByteArray());
//        }
//    }
//}