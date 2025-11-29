package com.example.springboot.scheduler;

import com.example.springboot.entity.ClassroomReservation;
import com.example.springboot.service.ClassroomReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class ReservationCleanupScheduler {

    @Autowired
    private ClassroomReservationService reservationService;

    // 每分钟执行一次检查
    // 在ReservationCleanupScheduler.java中
    // 在 ReservationCleanupScheduler.java 中
    @Scheduled(cron = "0 * * * * ?")  // 每分钟执行一次
    public void cleanupExpiredReservations() {
        // 直接获取List，无需处理Result包装
        List<ClassroomReservation> expiredReservations = reservationService.listExpiredPendingReservations();

        if (expiredReservations != null && !expiredReservations.isEmpty()) {
            for (ClassroomReservation reservation : expiredReservations) {
                reservationService.auditReservation(
                        reservation.getId(),
                        2,  // 新增状态：已过期
                        "预约时间已过，自动失效"
                );
            }
        }
    }
}