package com.example.springboot.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

@Component
public class ClassroomDateInitializer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init7DaysClassroomData() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar calendar = Calendar.getInstance();
            Date today = calendar.getTime();
            String todayStr = sdf.format(today);

            // 1. 保留修复后的删除逻辑（仅删7天前和7天后的数据）
            calendar.setTime(today);
            calendar.add(Calendar.DATE, -7);
            String sevenDaysAgo = sdf.format(calendar.getTime());
            calendar.setTime(today);
            calendar.add(Calendar.DATE, 7);
            String sevenDaysLater = sdf.format(calendar.getTime());
            String deleteSql = "DELETE FROM classroom WHERE date < ? OR date > ?";
            int deleteCount = jdbcTemplate.update(deleteSql, sevenDaysAgo, sevenDaysLater);
            System.out.println("【初始化】删除超期数据：" + deleteCount + "条");

            // 2. 定义所有需要生成的教室基础数据（统一模板）
            String[][] classrooms = {
                    // {classroom_id, building_id, classroom_name, seat_count, multimedia_type, description}
                    {"101", "J1", "J1-101多媒体教室", "60", "4K投影仪+智能音响+电脑", "学生专用"},
                    {"102", "J1", "J1-102多媒体教室", "45", "4K投影仪+智能黑板", "学生专用"},
                    {"103", "J1", "J1-103多媒体教室", "60", "4K投影仪+智能黑板", "学生专用"},
                    {"101", "J2", "J2-101多媒体教室", "50", "4K投影仪+智能黑板", "学生专用"},
                    {"201", "J2", "J2-201多媒体教室", "60", "4K投影仪+白板", "学生专用"},
                    {"202", "J2", "J2-202多媒体教室", "40", "4K投影仪", "学生专用"},
                    {"203", "J2", "J2-203讨论室", "30", "无", "学生专用"},
                    {"101", "J3", "J3-101多媒体教室", "80", "4K投影仪+智能黑板", "老师专用"},
                    {"102", "J3", "J3-102研讨室", "50", "4K投影仪+音响", "老师专用"},
                    {"101", "J4", "J4-101多媒体教室", "40", "4K投影仪+智能黑板", "老师专用"},
                    {"102", "J4", "J4-102会议室", "20", "无", "老师专用"}
            };

            // 3. 循环生成未来7天（含当天）的所有教室数据
            for (int i = 0; i < 7; i++) {
                calendar.setTime(today);
                calendar.add(Calendar.DATE, i);
                String targetDate = sdf.format(calendar.getTime());

                // 对每个教室，检查是否已存在，不存在则插入
                for (String[] cls : classrooms) {
                    String checkSql = "SELECT COUNT(*) FROM classroom " +
                            "WHERE building_id = ? AND classroom_id = ? AND date = ?";
                    long exists = jdbcTemplate.queryForObject(
                            checkSql, Long.class, cls[1], cls[0], targetDate
                    );

                    if (exists == 0) {
                        String insertSql = "INSERT INTO classroom " +
                                "(classroom_id, building_id, classroom_name, seat_count, " +
                                "multimedia_type, status, description, create_time, date) " +
                                "VALUES (?, ?, ?, ?, ?, 1, ?, NOW(), ?)";
                        jdbcTemplate.update(
                                insertSql,
                                cls[0], cls[1], cls[2], cls[3], cls[4], cls[5], targetDate
                        );
                    }
                }
                System.out.println("【初始化】已生成/验证 " + targetDate + " 的所有教室数据");
            }

        } catch (Exception e) {
            System.err.println("【初始化失败】：" + e.getMessage());
            e.printStackTrace();
        }
    }
}