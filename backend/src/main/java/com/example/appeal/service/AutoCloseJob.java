package com.example.appeal.service;

import com.example.appeal.intranet.AppealStatus;
import com.example.appeal.intranet.IntranetAppeal;
import com.example.appeal.intranet.IntranetAppealRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AutoCloseJob {

    private static final Logger logger = LoggerFactory.getLogger(AutoCloseJob.class);

    private final IntranetAppealRepository intranetRepo;
    private final int thresholdMinutes;

    public AutoCloseJob(IntranetAppealRepository intranetRepo,
                        @Value("${app.autoclose.threshold-minutes}") int thresholdMinutes) {
        this.intranetRepo = intranetRepo;
        this.thresholdMinutes = thresholdMinutes;
    }

    @Scheduled(fixedDelayString = "${app.autoclose.interval-ms}")
    @Transactional
    public void closeStaleAppeals() {
        logger.info("AutoCloseJob: Starting auto-close run...");
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(thresholdMinutes);
        List<IntranetAppeal> staleAppeals = intranetRepo.findByStatusAndRespondedAtBefore(
                AppealStatus.AWAITING_CUSTOMER, cutoff);

        int closedCount = 0;
        for (IntranetAppeal appeal : staleAppeals) {
            appeal.setStatus(AppealStatus.CLOSED);
            appeal.setClosedAt(LocalDateTime.now());
            appeal.setCloseReason("auto-closed: no customer reply");
            // lastUpdated is bumped via @PreUpdate/@PrePersist, but we can also set it explicitly
            appeal.setLastUpdated(LocalDateTime.now());

            intranetRepo.save(appeal);
            closedCount++;
            logger.info("AutoCloseJob: Auto-closed appeal ID {} (Customer: {}) due to inactivity.", appeal.getId(), appeal.getCustomerName());
        }

        logger.info("AutoCloseJob: Completed run. Auto-closed {} appeals.", closedCount);
    }
}
