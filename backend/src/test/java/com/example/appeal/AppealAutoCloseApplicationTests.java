package com.example.appeal;

import com.example.appeal.internet.InternetAppeal;
import com.example.appeal.internet.InternetAppealRepository;
import com.example.appeal.intranet.AppealStatus;
import com.example.appeal.intranet.IntranetAppeal;
import com.example.appeal.intranet.IntranetAppealRepository;
import com.example.appeal.service.AutoCloseJob;
import com.example.appeal.service.SyncJob;
import com.example.appeal.service.AppealService;
import com.example.appeal.dto.AppealSummaryDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class AppealAutoCloseApplicationTests {

    @Autowired
    private InternetAppealRepository internetRepo;

    @Autowired
    private IntranetAppealRepository intranetRepo;

    @Autowired
    private SyncJob syncJob;

    @Autowired
    private AutoCloseJob autoCloseJob;

    @Autowired
    private AppealService appealService;

    @Value("${app.autoclose.threshold-minutes}")
    private int thresholdMinutes;

    @Test
    public void contextLoads() {
    }

    @Test
    public void testSyncJobIdempotency() {
        intranetRepo.deleteAll();
        internetRepo.deleteAll();

        InternetAppeal i1 = new InternetAppeal();
        i1.setCustomerName("Test 1");
        i1.setSubject("Subject 1");
        i1.setMessage("Message 1");
        i1.setSubmittedAt(LocalDateTime.now());
        internetRepo.save(i1);

        InternetAppeal i2 = new InternetAppeal();
        i2.setCustomerName("Test 2");
        i2.setSubject("Subject 2");
        i2.setMessage("Message 2");
        i2.setSubmittedAt(LocalDateTime.now());
        internetRepo.save(i2);

        syncJob.syncNewAppeals();
        assertEquals(2, intranetRepo.count());

        syncJob.syncNewAppeals();
        assertEquals(2, intranetRepo.count());
    }

    @Test
    public void testAutoCloseJob() {
        intranetRepo.deleteAll();
        internetRepo.deleteAll();

        LocalDateTime now = LocalDateTime.now();

        // 1. AWAITING_CUSTOMER stale (over threshold) -> should close
        IntranetAppeal a1 = new IntranetAppeal();
        a1.setId(100L);
        a1.setCustomerName("Stale Customer");
        a1.setStatus(AppealStatus.AWAITING_CUSTOMER);
        a1.setRespondedAt(now.minusMinutes(thresholdMinutes + 5));
        intranetRepo.save(a1);

        // 2. AWAITING_CUSTOMER recent (under threshold) -> should stay awaiting
        IntranetAppeal a2 = new IntranetAppeal();
        a2.setId(200L);
        a2.setCustomerName("Recent Customer");
        a2.setStatus(AppealStatus.AWAITING_CUSTOMER);
        a2.setRespondedAt(now.minusMinutes(thresholdMinutes - 2));
        intranetRepo.save(a2);

        // 3. OPEN -> should stay open
        IntranetAppeal a3 = new IntranetAppeal();
        a3.setId(300L);
        a3.setCustomerName("Open Customer");
        a3.setStatus(AppealStatus.OPEN);
        intranetRepo.save(a3);

        // 4. CLOSED previously -> should stay closed
        IntranetAppeal a4 = new IntranetAppeal();
        a4.setId(400L);
        a4.setCustomerName("Closed Customer");
        a4.setStatus(AppealStatus.CLOSED);
        a4.setClosedAt(now.minusDays(1));
        a4.setCloseReason("resolved");
        intranetRepo.save(a4);

        autoCloseJob.closeStaleAppeals();

        IntranetAppeal updatedA1 = intranetRepo.findById(100L).orElseThrow();
        assertEquals(AppealStatus.CLOSED, updatedA1.getStatus());
        assertEquals("auto-closed: no customer reply", updatedA1.getCloseReason());
        assertNotNull(updatedA1.getClosedAt());

        IntranetAppeal updatedA2 = intranetRepo.findById(200L).orElseThrow();
        assertEquals(AppealStatus.AWAITING_CUSTOMER, updatedA2.getStatus());

        IntranetAppeal updatedA3 = intranetRepo.findById(300L).orElseThrow();
        assertEquals(AppealStatus.OPEN, updatedA3.getStatus());

        IntranetAppeal updatedA4 = intranetRepo.findById(400L).orElseThrow();
        assertEquals(AppealStatus.CLOSED, updatedA4.getStatus());
        assertEquals("resolved", updatedA4.getCloseReason());
    }

    @Test
    public void testMergeServiceStreams() {
        intranetRepo.deleteAll();
        internetRepo.deleteAll();

        InternetAppeal i = new InternetAppeal();
        i.setCustomerName("Internet Customer");
        i.setSubject("Internet Subject");
        i.setMessage("Message");
        i.setSubmittedAt(LocalDateTime.now());
        i = internetRepo.save(i);

        IntranetAppeal intra = new IntranetAppeal();
        intra.setId(i.getId());
        intra.setCustomerName("Intranet Name");
        intra.setSubject("Intranet Subject");
        intra.setStatus(AppealStatus.OPEN);
        intra.setLastUpdated(LocalDateTime.now());
        intranetRepo.save(intra);

        List<AppealSummaryDto> summaries = appealService.getStaffAppeals();
        assertEquals(1, summaries.size());
        AppealSummaryDto summary = summaries.get(0);
        assertEquals(i.getId(), summary.getId());
        assertEquals("Internet Customer", summary.getCustomerName());
        assertEquals("Internet Subject", summary.getSubject());
    }
}
