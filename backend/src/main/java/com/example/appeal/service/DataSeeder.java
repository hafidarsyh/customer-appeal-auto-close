package com.example.appeal.service;

import com.example.appeal.internet.InternetAppeal;
import com.example.appeal.internet.InternetAppealRepository;
import com.example.appeal.intranet.AppealStatus;
import com.example.appeal.intranet.IntranetAppeal;
import com.example.appeal.intranet.IntranetAppealRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final InternetAppealRepository internetRepo;
    private final IntranetAppealRepository intranetRepo;
    private final int thresholdMinutes;

    public DataSeeder(InternetAppealRepository internetRepo,
                      IntranetAppealRepository intranetRepo,
                      @Value("${app.autoclose.threshold-minutes}") int thresholdMinutes) {
        this.internetRepo = internetRepo;
        this.intranetRepo = intranetRepo;
        this.thresholdMinutes = thresholdMinutes;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("DataSeeder: Starting database seeding (threshold-minutes: {})...", thresholdMinutes);

        // Clear existing data (in case of schema updates/re-runs)
        internetRepo.deleteAll();
        intranetRepo.deleteAll();

        LocalDateTime now = LocalDateTime.now();

        // 1. Appeal 1: OPEN - in both DBs
        InternetAppeal i1 = new InternetAppeal();
        i1.setCustomerName("John Doe");
        i1.setSubject("Parking Ticket Appeal");
        i1.setMessage("I parked in a designated loading zone with my hazard lights on for only 2 minutes.");
        i1.setSubmittedAt(now.minusDays(3));
        i1 = internetRepo.save(i1);

        IntranetAppeal intra1 = new IntranetAppeal();
        intra1.setId(i1.getId());
        intra1.setCustomerName(i1.getCustomerName());
        intra1.setSubject(i1.getSubject());
        intra1.setMessage(i1.getMessage());
        intra1.setSubmittedAt(i1.getSubmittedAt());
        intra1.setStatus(AppealStatus.OPEN);
        intra1.setLastUpdated(now.minusDays(3));
        intranetRepo.save(intra1);

        // 2. Appeal 2: AWAITING_CUSTOMER - Responded recently (must NOT close)
        InternetAppeal i2 = new InternetAppeal();
        i2.setCustomerName("Jane Smith");
        i2.setSubject("Tree Trimming Request");
        i2.setMessage("The branches of the public tree are leaning on my roof. Please assist.");
        i2.setSubmittedAt(now.minusDays(5));
        i2 = internetRepo.save(i2);

        IntranetAppeal intra2 = new IntranetAppeal();
        intra2.setId(i2.getId());
        intra2.setCustomerName(i2.getCustomerName());
        intra2.setSubject(i2.getSubject());
        intra2.setMessage(i2.getMessage());
        intra2.setSubmittedAt(i2.getSubmittedAt());
        intra2.setStatus(AppealStatus.AWAITING_CUSTOMER);
        intra2.setOfficerResponse("We scheduled a contractor to inspect it next week. Please confirm if access is clear.");
        // Under threshold: e.g., 1/4 of threshold (2 mins -> 30 secs ago; 7 days -> 1.75 days ago)
        intra2.setRespondedAt(now.minusMinutes(thresholdMinutes / 4));
        intra2.setLastUpdated(intra2.getRespondedAt());
        intranetRepo.save(intra2);

        // 3. Appeal 3: AWAITING_CUSTOMER - Responded long ago (must auto-close)
        InternetAppeal i3 = new InternetAppeal();
        i3.setCustomerName("Bob Johnson");
        i3.setSubject("Noise Complaint");
        i3.setMessage("My neighbors are running a loud generator at 2 AM every day.");
        i3.setSubmittedAt(now.minusDays(10));
        i3 = internetRepo.save(i3);

        IntranetAppeal intra3 = new IntranetAppeal();
        intra3.setId(i3.getId());
        intra3.setCustomerName(i3.getCustomerName());
        intra3.setSubject(i3.getSubject());
        intra3.setMessage(i3.getMessage());
        intra3.setSubmittedAt(i3.getSubmittedAt());
        intra3.setStatus(AppealStatus.AWAITING_CUSTOMER);
        intra3.setOfficerResponse("An officer visited the address. Did you record any decibel level measurements?");
        // Over threshold: e.g., 2 times threshold (2 mins -> 4 mins ago; 7 days -> 14 days ago)
        intra3.setRespondedAt(now.minusMinutes(thresholdMinutes * 2));
        intra3.setLastUpdated(intra3.getRespondedAt());
        intranetRepo.save(intra3);

        // 4. Appeal 4: CLOSED - Auto-closed previously
        InternetAppeal i4 = new InternetAppeal();
        i4.setCustomerName("Alice Williams");
        i4.setSubject("Pothole Repair");
        i4.setMessage("Huge pothole on 5th avenue damaged my tire. Can you fix it?");
        i4.setSubmittedAt(now.minusDays(12));
        i4 = internetRepo.save(i4);

        IntranetAppeal intra4 = new IntranetAppeal();
        intra4.setId(i4.getId());
        intra4.setCustomerName(i4.getCustomerName());
        intra4.setSubject(i4.getSubject());
        intra4.setMessage(i4.getMessage());
        intra4.setSubmittedAt(i4.getSubmittedAt());
        intra4.setStatus(AppealStatus.CLOSED);
        intra4.setOfficerResponse("Please upload a picture of the pothole and receipt of tire repair.");
        intra4.setRespondedAt(now.minusDays(11));
        intra4.setClosedAt(now.minusDays(4));
        intra4.setCloseReason("auto-closed: no customer reply");
        intra4.setLastUpdated(intra4.getClosedAt());
        intranetRepo.save(intra4);

        // 5. Appeal 5: CLOSED - Resolved/manually closed
        InternetAppeal i5 = new InternetAppeal();
        i5.setCustomerName("Charlie Brown");
        i5.setSubject("Property Tax Inquiry");
        i5.setMessage("I received an incorrect assessment. Who should I contact?");
        i5.setSubmittedAt(now.minusDays(15));
        i5 = internetRepo.save(i5);

        IntranetAppeal intra5 = new IntranetAppeal();
        intra5.setId(i5.getId());
        intra5.setCustomerName(i5.getCustomerName());
        intra5.setSubject(i5.getSubject());
        intra5.setMessage(i5.getMessage());
        intra5.setSubmittedAt(i5.getSubmittedAt());
        intra5.setStatus(AppealStatus.CLOSED);
        intra5.setOfficerResponse("Please contact the Tax Assessor's office at 555-0199 or email tax@council.gov.");
        intra5.setRespondedAt(now.minusDays(14));
        intra5.setClosedAt(now.minusDays(7));
        intra5.setCloseReason("Resolved: officer response accepted");
        intra5.setLastUpdated(intra5.getClosedAt());
        intranetRepo.save(intra5);

        // 6. Appeal 6: ONLY IN INTERNET DB - Pending synchronization
        InternetAppeal i6 = new InternetAppeal();
        i6.setCustomerName("Diana Prince");
        i6.setSubject("Zoning Permit Appeal");
        i6.setMessage("I would like to dispute the denial of my garage building permit.");
        i6.setSubmittedAt(now.minusMinutes(5));
        internetRepo.save(i6);

        logger.info("DataSeeder: Database seeding completed. 6 internet appeals and 5 intranet appeals created.");
    }
}
