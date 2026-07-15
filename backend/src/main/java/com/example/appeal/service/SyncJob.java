package com.example.appeal.service;

import com.example.appeal.internet.InternetAppeal;
import com.example.appeal.internet.InternetAppealRepository;
import com.example.appeal.intranet.AppealStatus;
import com.example.appeal.intranet.IntranetAppeal;
import com.example.appeal.intranet.IntranetAppealRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class SyncJob {

    private static final Logger logger = LoggerFactory.getLogger(SyncJob.class);

    private final InternetAppealRepository internetRepo;
    private final IntranetAppealRepository intranetRepo;

    public SyncJob(InternetAppealRepository internetRepo, IntranetAppealRepository intranetRepo) {
        this.internetRepo = internetRepo;
        this.intranetRepo = intranetRepo;
    }

    @Scheduled(fixedDelayString = "${app.sync.interval-ms}")
    @Transactional
    public void syncNewAppeals() {
        logger.info("SyncJob: Starting synchronization run...");
        List<InternetAppeal> internetAppeals = internetRepo.findAll();
        int copiedCount = 0;

        for (InternetAppeal internetAppeal : internetAppeals) {
            if (!intranetRepo.existsById(internetAppeal.getId())) {
                IntranetAppeal intranetAppeal = new IntranetAppeal();
                intranetAppeal.setId(internetAppeal.getId());
                intranetAppeal.setCustomerName(internetAppeal.getCustomerName());
                intranetAppeal.setSubject(internetAppeal.getSubject());
                intranetAppeal.setMessage(internetAppeal.getMessage());
                intranetAppeal.setSubmittedAt(internetAppeal.getSubmittedAt());
                intranetAppeal.setStatus(AppealStatus.OPEN);
                intranetAppeal.setLastUpdated(LocalDateTime.now());

                intranetRepo.save(intranetAppeal);
                copiedCount++;
                logger.info("SyncJob: Copied appeal ID {} (Customer: {}) to intranet_db.", internetAppeal.getId(), internetAppeal.getCustomerName());
            }
        }

        logger.info("SyncJob: Completed run. Copied {} new appeals.", copiedCount);
    }
}
