/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 50743 (5.7.43-log)
 Source Host           : localhost:3306
 Source Schema         : smart_classroom_sys

 Target Server Type    : MySQL
 Target Server Version : 50743 (5.7.43-log)
 File Encoding         : 65001

 Date: 29/11/2025 14:09:51
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for adjust_room
-- ----------------------------
DROP TABLE IF EXISTS `adjust_room`;
CREATE TABLE `adjust_room`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '账号',
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '姓名',
  `currentroom_id` int(11) NOT NULL COMMENT '当前房间',
  `currentbed_id` int(11) NOT NULL COMMENT '当前床位号',
  `towardsroom_id` int(11) NOT NULL COMMENT '目标房间',
  `towardsbed_id` int(11) NOT NULL COMMENT '目标床位号',
  `state` enum('未处理','通过','驳回') CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '未处理' COMMENT '申请状态',
  `apply_time` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '申请时间',
  `finish_time` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '处理时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of adjust_room
-- ----------------------------
INSERT INTO `adjust_room` VALUES (2, 'stu1', '张三', 1101, 1, 1201, 1, '通过', '2023-09-17 14:35:02', '2023-09-19 23:07:21');
INSERT INTO `adjust_room` VALUES (9, 'stu1', '张三', 1201, 1, 1201, 3, '未处理', '2023-10-03 21:55:50', NULL);

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `username` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '密码',
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '姓名',
  `gender` enum('男','女') CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '男' COMMENT '性别',
  `age` int(11) NOT NULL COMMENT '年龄',
  `phone_num` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `status` tinyint(4) NULL DEFAULT NULL,
  PRIMARY KEY (`username`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of admin
-- ----------------------------
INSERT INTO `admin` VALUES ('admin', '123456', '大强', '男', 22, '14785412478', '2624052082@qq.com', 1);
INSERT INTO `admin` VALUES ('Atest', '123456', '测试管理员', '男', 22, '14785412478', NULL, 1);

-- ----------------------------
-- Table structure for building
-- ----------------------------
DROP TABLE IF EXISTS `building`;
CREATE TABLE `building`  (
  `building_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '教学楼唯一标识（如“J1”“J2”“实训楼A”）',
  `building_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '教学楼名称（如“第一教学楼”“实验楼”）',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '教学楼位置（如“学校东门北侧”）',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '备注（如“含多媒体教室30间”）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`building_id`) USING BTREE,
  INDEX `idx_building_name`(`building_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '教学楼信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of building
-- ----------------------------
INSERT INTO `building` VALUES ('J1', '第一教学楼', '学校南门入口东侧50米', '共5层，含30间多媒体教室，主要用于大一/大二基础课程', '2025-10-30 15:22:23');
INSERT INTO `building` VALUES ('J2', '第二教学楼', '学校南门入口西侧50米', '共4层，含25间多媒体教室，配备专业实验设备，用于专业课程教学', '2025-10-30 15:22:23');
INSERT INTO `building` VALUES ('J3', '第三教学楼', '学校图书馆北侧', '共3层，含10间大型阶梯教室（200人容量）和5间实训教室，用于讲座/实训课', '2025-10-30 15:22:23');
INSERT INTO `building` VALUES ('J4', '第四教学楼', '学校体育场东侧', '共6层，全楼配备最新多媒体设备（4K投影仪+智能黑板），用于重点课程和精品课教学', '2025-10-30 15:22:23');

-- ----------------------------
-- Table structure for classroom
-- ----------------------------
DROP TABLE IF EXISTS `classroom`;
CREATE TABLE `classroom`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `classroom_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '教室号（如“101”“203”，同一教学楼内唯一）',
  `building_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '所属教学楼ID（关联building表的building_id）',
  `classroom_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '教室全称（如“J1-101多媒体教室”）',
  `seat_count` int(11) NULL DEFAULT 0 COMMENT '座位数',
  `multimedia_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '多媒体设备配置（如“投影仪+音响+电脑”）',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '教室状态：1-可用，0-维护中',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '备注（如“4楼东侧，阶梯教室”）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `date` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '2025-10-31' COMMENT '预约日期（格式：YYYY-MM-DD）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_building_classroom_date`(`building_id`, `classroom_id`, `date`) USING BTREE,
  INDEX `fk_classroom_building`(`building_id`) USING BTREE,
  CONSTRAINT `classroom_ibfk_1` FOREIGN KEY (`building_id`) REFERENCES `building` (`building_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 441 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '教室信息表（含多媒体配置）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of classroom
-- ----------------------------
INSERT INTO `classroom` VALUES (287, '101', 'J1', 'J1-101多媒体教室', 60, '4K投影仪+智能音响+电脑', 1, '学生专用', '2025-11-17 11:08:20', '2025-11-22');
INSERT INTO `classroom` VALUES (288, '102', 'J1', 'J1-102多媒体教室', 45, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-17 11:08:20', '2025-11-22');
INSERT INTO `classroom` VALUES (289, '103', 'J1', 'J1-103多媒体教室', 60, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-17 11:08:20', '2025-11-22');
INSERT INTO `classroom` VALUES (290, '101', 'J2', 'J2-101多媒体教室', 50, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-17 11:08:20', '2025-11-22');
INSERT INTO `classroom` VALUES (291, '201', 'J2', 'J2-201多媒体教室', 60, '4K投影仪+白板', 1, '学生专用', '2025-11-17 11:08:20', '2025-11-22');
INSERT INTO `classroom` VALUES (292, '202', 'J2', 'J2-202多媒体教室', 40, '4K投影仪', 1, '学生专用', '2025-11-17 11:08:20', '2025-11-22');
INSERT INTO `classroom` VALUES (293, '203', 'J2', 'J2-203讨论室', 30, '无', 1, '学生专用', '2025-11-17 11:08:20', '2025-11-22');
INSERT INTO `classroom` VALUES (294, '101', 'J3', 'J3-101多媒体教室', 80, '4K投影仪+智能黑板', 1, '老师专用', '2025-11-17 11:08:20', '2025-11-22');
INSERT INTO `classroom` VALUES (295, '102', 'J3', 'J3-102研讨室', 50, '4K投影仪+音响', 1, '老师专用', '2025-11-17 11:08:20', '2025-11-22');
INSERT INTO `classroom` VALUES (296, '101', 'J4', 'J4-101多媒体教室', 40, '4K投影仪+智能黑板', 1, '老师专用', '2025-11-17 11:08:20', '2025-11-22');
INSERT INTO `classroom` VALUES (297, '102', 'J4', 'J4-102会议室', 20, '无', 1, '老师专用', '2025-11-17 11:08:20', '2025-11-22');
INSERT INTO `classroom` VALUES (298, '101', 'J1', 'J1-101多媒体教室', 60, '4K投影仪+智能音响+电脑', 1, '学生专用', '2025-11-17 11:08:20', '2025-11-23');
INSERT INTO `classroom` VALUES (299, '102', 'J1', 'J1-102多媒体教室', 45, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-17 11:08:20', '2025-11-23');
INSERT INTO `classroom` VALUES (300, '103', 'J1', 'J1-103多媒体教室', 60, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-17 11:08:20', '2025-11-23');
INSERT INTO `classroom` VALUES (301, '101', 'J2', 'J2-101多媒体教室', 50, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-17 11:08:20', '2025-11-23');
INSERT INTO `classroom` VALUES (302, '201', 'J2', 'J2-201多媒体教室', 60, '4K投影仪+白板', 1, '学生专用', '2025-11-17 11:08:20', '2025-11-23');
INSERT INTO `classroom` VALUES (303, '202', 'J2', 'J2-202多媒体教室', 40, '4K投影仪', 1, '学生专用', '2025-11-17 11:08:20', '2025-11-23');
INSERT INTO `classroom` VALUES (304, '203', 'J2', 'J2-203讨论室', 30, '无', 1, '学生专用', '2025-11-17 11:08:20', '2025-11-23');
INSERT INTO `classroom` VALUES (305, '101', 'J3', 'J3-101多媒体教室', 80, '4K投影仪+智能黑板', 1, '老师专用', '2025-11-17 11:08:20', '2025-11-23');
INSERT INTO `classroom` VALUES (306, '102', 'J3', 'J3-102研讨室', 50, '4K投影仪+音响', 1, '老师专用', '2025-11-17 11:08:20', '2025-11-23');
INSERT INTO `classroom` VALUES (307, '101', 'J4', 'J4-101多媒体教室', 40, '4K投影仪+智能黑板', 1, '老师专用', '2025-11-17 11:08:20', '2025-11-23');
INSERT INTO `classroom` VALUES (308, '102', 'J4', 'J4-102会议室', 20, '无', 1, '老师专用', '2025-11-17 11:08:20', '2025-11-23');
INSERT INTO `classroom` VALUES (309, '101', 'J1', 'J1-101多媒体教室', 60, '4K投影仪+智能音响+电脑', 1, '学生专用', '2025-11-19 15:46:26', '2025-11-24');
INSERT INTO `classroom` VALUES (310, '102', 'J1', 'J1-102多媒体教室', 45, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-19 15:46:26', '2025-11-24');
INSERT INTO `classroom` VALUES (311, '103', 'J1', 'J1-103多媒体教室', 60, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-19 15:46:26', '2025-11-24');
INSERT INTO `classroom` VALUES (312, '101', 'J2', 'J2-101多媒体教室', 50, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-19 15:46:26', '2025-11-24');
INSERT INTO `classroom` VALUES (313, '201', 'J2', 'J2-201多媒体教室', 60, '4K投影仪+白板', 1, '学生专用', '2025-11-19 15:46:26', '2025-11-24');
INSERT INTO `classroom` VALUES (314, '202', 'J2', 'J2-202多媒体教室', 40, '4K投影仪', 1, '学生专用', '2025-11-19 15:46:26', '2025-11-24');
INSERT INTO `classroom` VALUES (315, '203', 'J2', 'J2-203讨论室', 30, '无', 1, '学生专用', '2025-11-19 15:46:26', '2025-11-24');
INSERT INTO `classroom` VALUES (316, '101', 'J3', 'J3-101多媒体教室', 80, '4K投影仪+智能黑板', 1, '老师专用', '2025-11-19 15:46:26', '2025-11-24');
INSERT INTO `classroom` VALUES (317, '102', 'J3', 'J3-102研讨室', 50, '4K投影仪+音响', 1, '老师专用', '2025-11-19 15:46:26', '2025-11-24');
INSERT INTO `classroom` VALUES (318, '101', 'J4', 'J4-101多媒体教室', 40, '4K投影仪+智能黑板', 1, '老师专用', '2025-11-19 15:46:26', '2025-11-24');
INSERT INTO `classroom` VALUES (319, '102', 'J4', 'J4-102会议室', 20, '无', 1, '老师专用', '2025-11-19 15:46:26', '2025-11-24');
INSERT INTO `classroom` VALUES (320, '101', 'J1', 'J1-101多媒体教室', 60, '4K投影仪+智能音响+电脑', 1, '学生专用', '2025-11-19 15:46:26', '2025-11-25');
INSERT INTO `classroom` VALUES (321, '102', 'J1', 'J1-102多媒体教室', 45, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-19 15:46:26', '2025-11-25');
INSERT INTO `classroom` VALUES (322, '103', 'J1', 'J1-103多媒体教室', 60, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-19 15:46:26', '2025-11-25');
INSERT INTO `classroom` VALUES (323, '101', 'J2', 'J2-101多媒体教室', 50, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-19 15:46:26', '2025-11-25');
INSERT INTO `classroom` VALUES (324, '201', 'J2', 'J2-201多媒体教室', 60, '4K投影仪+白板', 1, '学生专用', '2025-11-19 15:46:26', '2025-11-25');
INSERT INTO `classroom` VALUES (325, '202', 'J2', 'J2-202多媒体教室', 40, '4K投影仪', 1, '学生专用', '2025-11-19 15:46:26', '2025-11-25');
INSERT INTO `classroom` VALUES (326, '203', 'J2', 'J2-203讨论室', 30, '无', 1, '学生专用', '2025-11-19 15:46:26', '2025-11-25');
INSERT INTO `classroom` VALUES (327, '101', 'J3', 'J3-101多媒体教室', 80, '4K投影仪+智能黑板', 1, '老师专用', '2025-11-19 15:46:26', '2025-11-25');
INSERT INTO `classroom` VALUES (328, '102', 'J3', 'J3-102研讨室', 50, '4K投影仪+音响', 1, '老师专用', '2025-11-19 15:46:26', '2025-11-25');
INSERT INTO `classroom` VALUES (329, '101', 'J4', 'J4-101多媒体教室', 40, '4K投影仪+智能黑板', 1, '老师专用', '2025-11-19 15:46:26', '2025-11-25');
INSERT INTO `classroom` VALUES (330, '102', 'J4', 'J4-102会议室', 20, '无', 1, '老师专用', '2025-11-19 15:46:26', '2025-11-25');
INSERT INTO `classroom` VALUES (331, '101', 'J1', 'J1-101多媒体教室', 60, '4K投影仪+智能音响+电脑', 1, '学生专用', '2025-11-20 13:53:20', '2025-11-26');
INSERT INTO `classroom` VALUES (332, '102', 'J1', 'J1-102多媒体教室', 45, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-20 13:53:20', '2025-11-26');
INSERT INTO `classroom` VALUES (333, '103', 'J1', 'J1-103多媒体教室', 60, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-20 13:53:20', '2025-11-26');
INSERT INTO `classroom` VALUES (334, '101', 'J2', 'J2-101多媒体教室', 50, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-20 13:53:20', '2025-11-26');
INSERT INTO `classroom` VALUES (335, '201', 'J2', 'J2-201多媒体教室', 60, '4K投影仪+白板', 1, '学生专用', '2025-11-20 13:53:20', '2025-11-26');
INSERT INTO `classroom` VALUES (336, '202', 'J2', 'J2-202多媒体教室', 40, '4K投影仪', 1, '学生专用', '2025-11-20 13:53:20', '2025-11-26');
INSERT INTO `classroom` VALUES (337, '203', 'J2', 'J2-203讨论室', 30, '无', 1, '学生专用', '2025-11-20 13:53:20', '2025-11-26');
INSERT INTO `classroom` VALUES (338, '101', 'J3', 'J3-101多媒体教室', 80, '4K投影仪+智能黑板', 1, '老师专用', '2025-11-20 13:53:20', '2025-11-26');
INSERT INTO `classroom` VALUES (339, '102', 'J3', 'J3-102研讨室', 50, '4K投影仪+音响', 1, '老师专用', '2025-11-20 13:53:20', '2025-11-26');
INSERT INTO `classroom` VALUES (340, '101', 'J4', 'J4-101多媒体教室', 40, '4K投影仪+智能黑板', 1, '老师专用', '2025-11-20 13:53:20', '2025-11-26');
INSERT INTO `classroom` VALUES (341, '102', 'J4', 'J4-102会议室', 20, '无', 1, '老师专用', '2025-11-20 13:53:20', '2025-11-26');
INSERT INTO `classroom` VALUES (342, '101', 'J1', 'J1-101多媒体教室', 60, '4K投影仪+智能音响+电脑', 1, '学生专用', '2025-11-24 08:39:22', '2025-11-27');
INSERT INTO `classroom` VALUES (343, '102', 'J1', 'J1-102多媒体教室', 45, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-24 08:39:22', '2025-11-27');
INSERT INTO `classroom` VALUES (344, '103', 'J1', 'J1-103多媒体教室', 60, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-24 08:39:22', '2025-11-27');
INSERT INTO `classroom` VALUES (345, '101', 'J2', 'J2-101多媒体教室', 50, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-24 08:39:22', '2025-11-27');
INSERT INTO `classroom` VALUES (346, '201', 'J2', 'J2-201多媒体教室', 60, '4K投影仪+白板', 1, '学生专用', '2025-11-24 08:39:22', '2025-11-27');
INSERT INTO `classroom` VALUES (347, '202', 'J2', 'J2-202多媒体教室', 40, '4K投影仪', 1, '学生专用', '2025-11-24 08:39:22', '2025-11-27');
INSERT INTO `classroom` VALUES (348, '203', 'J2', 'J2-203讨论室', 30, '无', 1, '学生专用', '2025-11-24 08:39:22', '2025-11-27');
INSERT INTO `classroom` VALUES (349, '101', 'J3', 'J3-101多媒体教室', 80, '4K投影仪+智能黑板', 1, '老师专用', '2025-11-24 08:39:22', '2025-11-27');
INSERT INTO `classroom` VALUES (350, '102', 'J3', 'J3-102研讨室', 50, '4K投影仪+音响', 1, '老师专用', '2025-11-24 08:39:22', '2025-11-27');
INSERT INTO `classroom` VALUES (351, '101', 'J4', 'J4-101多媒体教室', 40, '4K投影仪+智能黑板', 1, '老师专用', '2025-11-24 08:39:22', '2025-11-27');
INSERT INTO `classroom` VALUES (352, '102', 'J4', 'J4-102会议室', 20, '无', 1, '老师专用', '2025-11-24 08:39:22', '2025-11-27');
INSERT INTO `classroom` VALUES (353, '101', 'J1', 'J1-101多媒体教室', 60, '4K投影仪+智能音响+电脑', 1, '学生专用', '2025-11-24 08:39:22', '2025-11-28');
INSERT INTO `classroom` VALUES (354, '102', 'J1', 'J1-102多媒体教室', 45, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-24 08:39:22', '2025-11-28');
INSERT INTO `classroom` VALUES (355, '103', 'J1', 'J1-103多媒体教室', 60, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-24 08:39:22', '2025-11-28');
INSERT INTO `classroom` VALUES (356, '101', 'J2', 'J2-101多媒体教室', 50, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-24 08:39:22', '2025-11-28');
INSERT INTO `classroom` VALUES (357, '201', 'J2', 'J2-201多媒体教室', 60, '4K投影仪+白板', 1, '学生专用', '2025-11-24 08:39:22', '2025-11-28');
INSERT INTO `classroom` VALUES (358, '202', 'J2', 'J2-202多媒体教室', 40, '4K投影仪', 1, '学生专用', '2025-11-24 08:39:22', '2025-11-28');
INSERT INTO `classroom` VALUES (359, '203', 'J2', 'J2-203讨论室', 30, '无', 1, '学生专用', '2025-11-24 08:39:22', '2025-11-28');
INSERT INTO `classroom` VALUES (360, '101', 'J3', 'J3-101多媒体教室', 80, '4K投影仪+智能黑板', 1, '老师专用', '2025-11-24 08:39:22', '2025-11-28');
INSERT INTO `classroom` VALUES (361, '102', 'J3', 'J3-102研讨室', 50, '4K投影仪+音响', 1, '老师专用', '2025-11-24 08:39:22', '2025-11-28');
INSERT INTO `classroom` VALUES (362, '101', 'J4', 'J4-101多媒体教室', 40, '4K投影仪+智能黑板', 1, '老师专用', '2025-11-24 08:39:22', '2025-11-28');
INSERT INTO `classroom` VALUES (363, '102', 'J4', 'J4-102会议室', 20, '无', 1, '老师专用', '2025-11-24 08:39:22', '2025-11-28');
INSERT INTO `classroom` VALUES (364, '101', 'J1', 'J1-101多媒体教室', 60, '4K投影仪+智能音响+电脑', 1, '学生专用', '2025-11-24 08:39:22', '2025-11-29');
INSERT INTO `classroom` VALUES (365, '102', 'J1', 'J1-102多媒体教室', 45, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-24 08:39:22', '2025-11-29');
INSERT INTO `classroom` VALUES (366, '103', 'J1', 'J1-103多媒体教室', 60, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-24 08:39:22', '2025-11-29');
INSERT INTO `classroom` VALUES (367, '101', 'J2', 'J2-101多媒体教室', 50, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-24 08:39:22', '2025-11-29');
INSERT INTO `classroom` VALUES (368, '201', 'J2', 'J2-201多媒体教室', 60, '4K投影仪+白板', 1, '学生专用', '2025-11-24 08:39:22', '2025-11-29');
INSERT INTO `classroom` VALUES (369, '202', 'J2', 'J2-202多媒体教室', 40, '4K投影仪', 1, '学生专用', '2025-11-24 08:39:22', '2025-11-29');
INSERT INTO `classroom` VALUES (370, '203', 'J2', 'J2-203讨论室', 30, '无', 1, '学生专用', '2025-11-24 08:39:22', '2025-11-29');
INSERT INTO `classroom` VALUES (371, '101', 'J3', 'J3-101多媒体教室', 80, '4K投影仪+智能黑板', 1, '老师专用', '2025-11-24 08:39:22', '2025-11-29');
INSERT INTO `classroom` VALUES (372, '102', 'J3', 'J3-102研讨室', 50, '4K投影仪+音响', 1, '老师专用', '2025-11-24 08:39:22', '2025-11-29');
INSERT INTO `classroom` VALUES (373, '101', 'J4', 'J4-101多媒体教室', 40, '4K投影仪+智能黑板', 1, '老师专用', '2025-11-24 08:39:22', '2025-11-29');
INSERT INTO `classroom` VALUES (374, '102', 'J4', 'J4-102会议室', 20, '无', 1, '老师专用', '2025-11-24 08:39:22', '2025-11-29');
INSERT INTO `classroom` VALUES (375, '101', 'J1', 'J1-101多媒体教室', 60, '4K投影仪+智能音响+电脑', 1, '学生专用', '2025-11-24 08:39:22', '2025-11-30');
INSERT INTO `classroom` VALUES (376, '102', 'J1', 'J1-102多媒体教室', 45, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-24 08:39:22', '2025-11-30');
INSERT INTO `classroom` VALUES (377, '103', 'J1', 'J1-103多媒体教室', 60, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-24 08:39:22', '2025-11-30');
INSERT INTO `classroom` VALUES (378, '101', 'J2', 'J2-101多媒体教室', 50, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-24 08:39:22', '2025-11-30');
INSERT INTO `classroom` VALUES (379, '201', 'J2', 'J2-201多媒体教室', 60, '4K投影仪+白板', 1, '学生专用', '2025-11-24 08:39:22', '2025-11-30');
INSERT INTO `classroom` VALUES (380, '202', 'J2', 'J2-202多媒体教室', 40, '4K投影仪', 1, '学生专用', '2025-11-24 08:39:22', '2025-11-30');
INSERT INTO `classroom` VALUES (381, '203', 'J2', 'J2-203讨论室', 30, '无', 1, '学生专用', '2025-11-24 08:39:22', '2025-11-30');
INSERT INTO `classroom` VALUES (382, '101', 'J3', 'J3-101多媒体教室', 80, '4K投影仪+智能黑板', 1, '老师专用', '2025-11-24 08:39:22', '2025-11-30');
INSERT INTO `classroom` VALUES (383, '102', 'J3', 'J3-102研讨室', 50, '4K投影仪+音响', 1, '老师专用', '2025-11-24 08:39:22', '2025-11-30');
INSERT INTO `classroom` VALUES (384, '101', 'J4', 'J4-101多媒体教室', 40, '4K投影仪+智能黑板', 1, '老师专用', '2025-11-24 08:39:22', '2025-11-30');
INSERT INTO `classroom` VALUES (385, '102', 'J4', 'J4-102会议室', 20, '无', 1, '老师专用', '2025-11-24 08:39:22', '2025-11-30');
INSERT INTO `classroom` VALUES (386, '101', 'J1', 'J1-101多媒体教室', 60, '4K投影仪+智能音响+电脑', 1, '学生专用', '2025-11-25 18:20:29', '2025-12-01');
INSERT INTO `classroom` VALUES (387, '102', 'J1', 'J1-102多媒体教室', 45, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-25 18:20:29', '2025-12-01');
INSERT INTO `classroom` VALUES (388, '103', 'J1', 'J1-103多媒体教室', 60, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-25 18:20:29', '2025-12-01');
INSERT INTO `classroom` VALUES (389, '101', 'J2', 'J2-101多媒体教室', 50, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-25 18:20:29', '2025-12-01');
INSERT INTO `classroom` VALUES (390, '201', 'J2', 'J2-201多媒体教室', 60, '4K投影仪+白板', 1, '学生专用', '2025-11-25 18:20:29', '2025-12-01');
INSERT INTO `classroom` VALUES (391, '202', 'J2', 'J2-202多媒体教室', 40, '4K投影仪', 1, '学生专用', '2025-11-25 18:20:29', '2025-12-01');
INSERT INTO `classroom` VALUES (392, '203', 'J2', 'J2-203讨论室', 30, '无', 1, '学生专用', '2025-11-25 18:20:29', '2025-12-01');
INSERT INTO `classroom` VALUES (393, '101', 'J3', 'J3-101多媒体教室', 80, '4K投影仪+智能黑板', 1, '老师专用', '2025-11-25 18:20:29', '2025-12-01');
INSERT INTO `classroom` VALUES (394, '102', 'J3', 'J3-102研讨室', 50, '4K投影仪+音响', 1, '老师专用', '2025-11-25 18:20:29', '2025-12-01');
INSERT INTO `classroom` VALUES (395, '101', 'J4', 'J4-101多媒体教室', 40, '4K投影仪+智能黑板', 1, '老师专用', '2025-11-25 18:20:29', '2025-12-01');
INSERT INTO `classroom` VALUES (396, '102', 'J4', 'J4-102会议室', 20, '无', 1, '老师专用', '2025-11-25 18:20:29', '2025-12-01');
INSERT INTO `classroom` VALUES (397, '101', 'J1', 'J1-101多媒体教室', 60, '4K投影仪+智能音响+电脑', 1, '学生专用', '2025-11-29 11:04:28', '2025-12-02');
INSERT INTO `classroom` VALUES (398, '102', 'J1', 'J1-102多媒体教室', 45, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-29 11:04:28', '2025-12-02');
INSERT INTO `classroom` VALUES (399, '103', 'J1', 'J1-103多媒体教室', 60, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-29 11:04:28', '2025-12-02');
INSERT INTO `classroom` VALUES (400, '101', 'J2', 'J2-101多媒体教室', 50, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-29 11:04:28', '2025-12-02');
INSERT INTO `classroom` VALUES (401, '201', 'J2', 'J2-201多媒体教室', 60, '4K投影仪+白板', 1, '学生专用', '2025-11-29 11:04:28', '2025-12-02');
INSERT INTO `classroom` VALUES (402, '202', 'J2', 'J2-202多媒体教室', 40, '4K投影仪', 1, '学生专用', '2025-11-29 11:04:28', '2025-12-02');
INSERT INTO `classroom` VALUES (403, '203', 'J2', 'J2-203讨论室', 30, '无', 1, '学生专用', '2025-11-29 11:04:28', '2025-12-02');
INSERT INTO `classroom` VALUES (404, '101', 'J3', 'J3-101多媒体教室', 80, '4K投影仪+智能黑板', 1, '老师专用', '2025-11-29 11:04:28', '2025-12-02');
INSERT INTO `classroom` VALUES (405, '102', 'J3', 'J3-102研讨室', 50, '4K投影仪+音响', 1, '老师专用', '2025-11-29 11:04:28', '2025-12-02');
INSERT INTO `classroom` VALUES (406, '101', 'J4', 'J4-101多媒体教室', 40, '4K投影仪+智能黑板', 1, '老师专用', '2025-11-29 11:04:28', '2025-12-02');
INSERT INTO `classroom` VALUES (407, '102', 'J4', 'J4-102会议室', 20, '无', 1, '老师专用', '2025-11-29 11:04:28', '2025-12-02');
INSERT INTO `classroom` VALUES (408, '101', 'J1', 'J1-101多媒体教室', 60, '4K投影仪+智能音响+电脑', 1, '学生专用', '2025-11-29 11:04:28', '2025-12-03');
INSERT INTO `classroom` VALUES (409, '102', 'J1', 'J1-102多媒体教室', 45, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-29 11:04:28', '2025-12-03');
INSERT INTO `classroom` VALUES (410, '103', 'J1', 'J1-103多媒体教室', 60, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-29 11:04:28', '2025-12-03');
INSERT INTO `classroom` VALUES (411, '101', 'J2', 'J2-101多媒体教室', 50, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-29 11:04:28', '2025-12-03');
INSERT INTO `classroom` VALUES (412, '201', 'J2', 'J2-201多媒体教室', 60, '4K投影仪+白板', 1, '学生专用', '2025-11-29 11:04:28', '2025-12-03');
INSERT INTO `classroom` VALUES (413, '202', 'J2', 'J2-202多媒体教室', 40, '4K投影仪', 1, '学生专用', '2025-11-29 11:04:28', '2025-12-03');
INSERT INTO `classroom` VALUES (414, '203', 'J2', 'J2-203讨论室', 30, '无', 1, '学生专用', '2025-11-29 11:04:28', '2025-12-03');
INSERT INTO `classroom` VALUES (415, '101', 'J3', 'J3-101多媒体教室', 80, '4K投影仪+智能黑板', 1, '老师专用', '2025-11-29 11:04:28', '2025-12-03');
INSERT INTO `classroom` VALUES (416, '102', 'J3', 'J3-102研讨室', 50, '4K投影仪+音响', 1, '老师专用', '2025-11-29 11:04:28', '2025-12-03');
INSERT INTO `classroom` VALUES (417, '101', 'J4', 'J4-101多媒体教室', 40, '4K投影仪+智能黑板', 1, '老师专用', '2025-11-29 11:04:28', '2025-12-03');
INSERT INTO `classroom` VALUES (418, '102', 'J4', 'J4-102会议室', 20, '无', 1, '老师专用', '2025-11-29 11:04:28', '2025-12-03');
INSERT INTO `classroom` VALUES (419, '101', 'J1', 'J1-101多媒体教室', 60, '4K投影仪+智能音响+电脑', 1, '学生专用', '2025-11-29 11:04:28', '2025-12-04');
INSERT INTO `classroom` VALUES (420, '102', 'J1', 'J1-102多媒体教室', 45, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-29 11:04:28', '2025-12-04');
INSERT INTO `classroom` VALUES (421, '103', 'J1', 'J1-103多媒体教室', 60, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-29 11:04:28', '2025-12-04');
INSERT INTO `classroom` VALUES (422, '101', 'J2', 'J2-101多媒体教室', 50, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-29 11:04:28', '2025-12-04');
INSERT INTO `classroom` VALUES (423, '201', 'J2', 'J2-201多媒体教室', 60, '4K投影仪+白板', 1, '学生专用', '2025-11-29 11:04:28', '2025-12-04');
INSERT INTO `classroom` VALUES (424, '202', 'J2', 'J2-202多媒体教室', 40, '4K投影仪', 1, '学生专用', '2025-11-29 11:04:28', '2025-12-04');
INSERT INTO `classroom` VALUES (425, '203', 'J2', 'J2-203讨论室', 30, '无', 1, '学生专用', '2025-11-29 11:04:28', '2025-12-04');
INSERT INTO `classroom` VALUES (426, '101', 'J3', 'J3-101多媒体教室', 80, '4K投影仪+智能黑板', 1, '老师专用', '2025-11-29 11:04:28', '2025-12-04');
INSERT INTO `classroom` VALUES (427, '102', 'J3', 'J3-102研讨室', 50, '4K投影仪+音响', 1, '老师专用', '2025-11-29 11:04:28', '2025-12-04');
INSERT INTO `classroom` VALUES (428, '101', 'J4', 'J4-101多媒体教室', 40, '4K投影仪+智能黑板', 1, '老师专用', '2025-11-29 11:04:28', '2025-12-04');
INSERT INTO `classroom` VALUES (429, '102', 'J4', 'J4-102会议室', 20, '无', 1, '老师专用', '2025-11-29 11:04:28', '2025-12-04');
INSERT INTO `classroom` VALUES (430, '101', 'J1', 'J1-101多媒体教室', 60, '4K投影仪+智能音响+电脑', 1, '学生专用', '2025-11-29 11:04:28', '2025-12-05');
INSERT INTO `classroom` VALUES (431, '102', 'J1', 'J1-102多媒体教室', 45, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-29 11:04:28', '2025-12-05');
INSERT INTO `classroom` VALUES (432, '103', 'J1', 'J1-103多媒体教室', 60, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-29 11:04:28', '2025-12-05');
INSERT INTO `classroom` VALUES (433, '101', 'J2', 'J2-101多媒体教室', 50, '4K投影仪+智能黑板', 1, '学生专用', '2025-11-29 11:04:28', '2025-12-05');
INSERT INTO `classroom` VALUES (434, '201', 'J2', 'J2-201多媒体教室', 60, '4K投影仪+白板', 1, '学生专用', '2025-11-29 11:04:28', '2025-12-05');
INSERT INTO `classroom` VALUES (435, '202', 'J2', 'J2-202多媒体教室', 40, '4K投影仪', 1, '学生专用', '2025-11-29 11:04:28', '2025-12-05');
INSERT INTO `classroom` VALUES (436, '203', 'J2', 'J2-203讨论室', 30, '无', 1, '学生专用', '2025-11-29 11:04:28', '2025-12-05');
INSERT INTO `classroom` VALUES (437, '101', 'J3', 'J3-101多媒体教室', 80, '4K投影仪+智能黑板', 1, '老师专用', '2025-11-29 11:04:28', '2025-12-05');
INSERT INTO `classroom` VALUES (438, '102', 'J3', 'J3-102研讨室', 50, '4K投影仪+音响', 1, '老师专用', '2025-11-29 11:04:28', '2025-12-05');
INSERT INTO `classroom` VALUES (439, '101', 'J4', 'J4-101多媒体教室', 40, '4K投影仪+智能黑板', 1, '老师专用', '2025-11-29 11:04:28', '2025-12-05');
INSERT INTO `classroom` VALUES (440, '102', 'J4', 'J4-102会议室', 20, '无', 1, '老师专用', '2025-11-29 11:04:28', '2025-12-05');

-- ----------------------------
-- Table structure for classroom_repair
-- ----------------------------
DROP TABLE IF EXISTS `classroom_repair`;
CREATE TABLE `classroom_repair`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `reporter_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '报修人ID（学号/工号）',
  `reporter_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '报修人姓名',
  `reporter_role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '报修人角色（stu/teacher）',
  `building_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '教学楼ID（如J1）',
  `classroom_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '教室号',
  `device_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '设备类型（如投影仪）',
  `problem_detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '问题描述',
  `status` int(1) NOT NULL DEFAULT 0 COMMENT '报修状态（0-未处理，1-处理中，2-已解决）',
  `submit_time` datetime NOT NULL COMMENT '提交时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '最后更新时间',
  `handler_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '处理人ID（管理员/宿管学号/工号）',
  `handler_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '处理人姓名',
  `handle_time` datetime NULL DEFAULT NULL COMMENT '处理时间',
  `handle_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '处理结果（维修说明/解决方案）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '教室设备报修记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of classroom_repair
-- ----------------------------
INSERT INTO `classroom_repair` VALUES (1, '202303002', NULL, 'stu', 'J1', '103', '投影仪', 'sssssssssssssssssssss', 2, '2025-11-17 17:38:39', NULL, 'Atest', '测试管理员', '2025-11-20 19:09:47', '无');
INSERT INTO `classroom_repair` VALUES (2, '202303002', NULL, 'stu', 'J1', '102', '投影仪', 'sssssssssssssssssss', 2, '2025-11-17 17:52:26', NULL, 'Atest', '测试管理员', '2025-11-20 19:20:24', 'ssssssssssssssss');
INSERT INTO `classroom_repair` VALUES (3, '202205001', '孙十二', 'stu', 'J1', '103', '投影仪', 'ssssssssssssssssssssssssssssss', 2, '2025-11-17 22:34:20', NULL, 'Atest', '测试管理员', '2025-11-20 19:20:28', 'ssssssssssssssssssssssss');
INSERT INTO `classroom_repair` VALUES (4, '202205001', '孙十二', 'stu', 'J1', '103', '投影仪', 'sssssssssssssssssss', 2, '2025-11-20 19:12:17', NULL, 'Atest', '测试管理员', '2025-11-20 19:15:36', '解决了');
INSERT INTO `classroom_repair` VALUES (5, '01', '孙权', 'teacher', 'J3', '101', '音响设备', 'sssssssssssssssssss', 1, '2025-11-20 19:20:07', NULL, 'Atest', '测试管理员', '2025-11-20 19:20:19', 'sssssssssssss');

-- ----------------------------
-- Table structure for classroom_reservation
-- ----------------------------
DROP TABLE IF EXISTS `classroom_reservation`;
CREATE TABLE `classroom_reservation`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '预约ID（自增主键）',
  `username` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '预约学生学号（关联学生表username）',
  `reserver_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '学生姓名（冗余存储）',
  `building_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'J1' COMMENT '教学楼ID（固定为1号教学楼J1）',
  `classroom_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '教室号（如101，关联教室表classroom_id）',
  `reserve_date` date NULL DEFAULT NULL,
  `start_time` time NULL DEFAULT NULL,
  `end_time` time NULL DEFAULT NULL,
  `reserve_reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '预约理由',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '预约状态：1-已通过，0-待审核',
  `create_time` datetime NULL DEFAULT NULL,
  `identity` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `auditor` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '审批人用户名',
  `auditor_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '审批人姓名',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审批时间',
  `admin_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '审批意见/拒绝理由',
  `checkin_time` datetime NULL DEFAULT NULL COMMENT '签到时间',
  `checkout_time` datetime NULL DEFAULT NULL COMMENT '签退时间',
  `check_status` tinyint(4) NULL DEFAULT 0 COMMENT '签到状态：0-未签到，1-已签到，2-已签退',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_date_classroom`(`reserve_date`, `classroom_id`) USING BTREE,
  INDEX `idx_username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 64 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '1号教学楼教室预约表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of classroom_reservation
-- ----------------------------
INSERT INTO `classroom_reservation` VALUES (57, 'T001', '孙权', 'J3', '101', '2025-11-10', '20:00:00', '22:00:00', '水水水水水水水水水水水水水水水水水水水', 1, '2025-11-10 19:17:42', 'teacher', 'Manager1', NULL, '2025-11-10 19:17:56', '无', '2025-11-10 19:30:26', '2025-11-10 21:41:59', 2);
INSERT INTO `classroom_reservation` VALUES (58, '01', '孙权', 'J3', '101', '2025-11-11', '14:00:00', '16:00:00', 'ssssssssssssssssssssssss', 2, '2025-11-11 10:41:34', 'teacher', 'system', NULL, '2025-11-11 14:01:00', '预约时间已过，自动失效', NULL, NULL, 0);
INSERT INTO `classroom_reservation` VALUES (60, '01', '孙权', 'J3', '101', '2025-11-12', '08:00:00', '10:00:00', 'testtesttest', 2, '2025-11-11 22:13:46', 'teacher', 'system', NULL, '2025-11-17 11:09:00', '预约时间已过，自动失效', NULL, NULL, 0);
INSERT INTO `classroom_reservation` VALUES (61, '202303002', '周八', 'J1', '101', '2025-11-12', '10:00:00', '12:00:00', 'testtesttest', 1, '2025-11-11 22:15:11', 'stu', 'Manager1', NULL, '2025-11-11 22:30:28', '无', NULL, NULL, 0);
INSERT INTO `classroom_reservation` VALUES (62, '202303002', '周八', 'J1', '101', '2025-11-17', '14:00:00', '16:00:00', 'xsssssssssssssssss', 2, '2025-11-17 11:44:09', 'stu', 'system', NULL, '2025-11-17 14:01:45', '预约时间已过，自动失效', NULL, NULL, 0);
INSERT INTO `classroom_reservation` VALUES (63, '202205001', '孙十二', 'J1', '101', '2025-11-20', '08:00:00', '10:00:00', 'testtesttesttesttest', 2, '2025-11-19 19:47:24', 'stu', 'system', NULL, '2025-11-20 13:54:00', '预约时间已过，自动失效', NULL, NULL, 0);

-- ----------------------------
-- Table structure for manager
-- ----------------------------
DROP TABLE IF EXISTS `manager`;
CREATE TABLE `manager`  (
  `username` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '123456' COMMENT '密码',
  `dormbuild_id` int(11) NOT NULL COMMENT '所管理的宿舍楼栋号',
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '名字',
  `gender` enum('男','女') CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '男' COMMENT '性别',
  `age` int(11) NOT NULL COMMENT '年龄',
  `phone_num` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `status` tinyint(4) NULL DEFAULT NULL,
  PRIMARY KEY (`username`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of manager
-- ----------------------------
INSERT INTO `manager` VALUES ('Manager1', '123456', 1, '张三', '男', 35, '15222223333', '12@email.com', 1);
INSERT INTO `manager` VALUES ('Manager2', '123456', 2, '李四', '女', 55, '15333332222', NULL, 1);
INSERT INTO `manager` VALUES ('Manager3', '123456', 3, '王五', '男', 38, '15855552222', NULL, 1);
INSERT INTO `manager` VALUES ('Manager4', '123456', 4, '赵花', '女', 40, '15877776666', NULL, 1);
INSERT INTO `manager` VALUES ('Mtest', '123456', 2, '宿管测试', '男', 22, '15899999999', NULL, 1);

-- ----------------------------
-- Table structure for new_student
-- ----------------------------
DROP TABLE IF EXISTS `new_student`;
CREATE TABLE `new_student`  (
  `username` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '学号（唯一标识）',
  `password` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '123456' COMMENT '密码（MD5加密，默认123456）',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '学生姓名',
  `major` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '专业（如“软件工程”）',
  `grade` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '年级（如“2022级”）',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系电话（11位手机号）',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
  `identity` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'stu',
  PRIMARY KEY (`username`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学生用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of new_student
-- ----------------------------
INSERT INTO `new_student` VALUES ('202101001', '123456', '钱十', '电子信息工程', '2021级', '13800138009', 1, 'stu');
INSERT INTO `new_student` VALUES ('202201001', '123456', '张三', '软件工程', '2022级', '13800138001', 1, 'stu');
INSERT INTO `new_student` VALUES ('202201002', '123456', '李四', '软件工程', '2022级', '13800138002', 1, 'stu');
INSERT INTO `new_student` VALUES ('202202001', '123456', '王五', '计算机科学与技术', '2022级', '13800138003', 1, 'stu');
INSERT INTO `new_student` VALUES ('202205001', '123456', '孙十二', '网络工程', '2022级', NULL, 1, 'stu');
INSERT INTO `new_student` VALUES ('202303001', '123456', '孙七', '数据科学与大数据技术', '2023级', '13800138005', 1, 'stu');
INSERT INTO `new_student` VALUES ('202303002', '123456', '周八', '数据科学与大数据技术', '2023级', '13800138006', 1, 'stu');
INSERT INTO `new_student` VALUES ('202304001', '123456', '吴九', '人工智能', '2023级', '13800138007', 1, 'stu');
INSERT INTO `new_student` VALUES ('202304002', '123456', '郑十', '人工智能', '2023级', '13800138008', 1, 'stu');

-- ----------------------------
-- Table structure for notice
-- ----------------------------
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '主题',
  `content` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '内容',
  `author` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '作者',
  `release_time` datetime NOT NULL COMMENT '发布时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of notice
-- ----------------------------
INSERT INTO `notice` VALUES (1, '入冬提醒', '<p>近期我校所在地区天气逐渐降低，同学们要注意多穿衣服，少熬夜，避免感染风寒</p>', '大强', '2023-11-01 00:17:27');
INSERT INTO `notice` VALUES (2, '关于宿舍卫生的新规定', '<p>学生公寓是学生们主要的生活区域，兼具休息、学习、交际等多种功能，是培养、提升学生全面素质不可或缺的重要阵地。为了培养学生良好的行为素养和生活习惯，我们实行宿舍长内务准军事化管理，切实把学生公寓建成学生自我教育，自我管理和自我服务的家园。</p>', '张三', '2023-02-14 00:02:59');
INSERT INTO `notice` VALUES (3, 'test', '<p>test</p>', '大强', '2025-10-30 00:00:00');
INSERT INTO `notice` VALUES (4, '无', '<p>测试</p>', '大强', '2025-11-19 16:15:55');

-- ----------------------------
-- Table structure for repair
-- ----------------------------
DROP TABLE IF EXISTS `repair`;
CREATE TABLE `repair`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '订单编号',
  `repairer` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '报修人',
  `dormbuild_id` int(11) NOT NULL COMMENT '报修宿舍楼',
  `dormroom_id` int(11) NOT NULL COMMENT '报修宿舍房间号',
  `title` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '表单标题',
  `content` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '表单内容',
  `state` enum('完成','未完成') CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '未完成' COMMENT '订单状态（是否维修完成）',
  `order_buildtime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '订单创建时间',
  `order_finishtime` datetime NULL DEFAULT NULL COMMENT '订单完成时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of repair
-- ----------------------------
INSERT INTO `repair` VALUES (1, '强强', 1, 1101, '水龙头损坏', '水龙头损坏，请来1-1101宿舍修理', '完成', '2023-11-11 22:52:24', '2023-11-17 14:35:02');
INSERT INTO `repair` VALUES (2, '张三', 1, 1101, '阳台漏水', '阳台使用时会漏水请来修理', '未完成', '2023-10-14 20:37:35', NULL);

-- ----------------------------
-- Table structure for teacher
-- ----------------------------
DROP TABLE IF EXISTS `teacher`;
CREATE TABLE `teacher`  (
  `username` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '教师工号（唯一标识）',
  `password` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '123456' COMMENT '密码（MD5加密）',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '教师姓名',
  `department` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '所属部门（如“计算机学院”）',
  `title` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '职称（如“讲师”“教授”）',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系电话',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
  `identity` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'teacher',
  PRIMARY KEY (`username`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of teacher
-- ----------------------------
INSERT INTO `teacher` VALUES ('T001', 'e10adc3949ba59abbe56e057f20f883e', '测试中', '计算机学院', '助教', '18898672066', 1, 'teacher');
INSERT INTO `teacher` VALUES ('T002', '123456', '测试', '计算机科学', '讲师', '18899672068', 1, 'teacher');
INSERT INTO `teacher` VALUES ('T003', '123456', '孙国亮', '物理学院', '讲师', '16927468286', 1, 'teacher');
INSERT INTO `teacher` VALUES ('T004', '123456', '孙立', '电子信息学院', '讲师', '19867252967', 1, 'teacher');

SET FOREIGN_KEY_CHECKS = 1;
