package com.example.appeal.service;

import com.example.appeal.dto.AppealDetailDto;
import com.example.appeal.dto.AppealSummaryDto;
import com.example.appeal.internet.InternetAppeal;
import com.example.appeal.internet.InternetAppealRepository;
import com.example.appeal.intranet.AppealStatus;
import com.example.appeal.intranet.IntranetAppeal;
import com.example.appeal.intranet.IntranetAppealRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AppealService {

    private final InternetAppealRepository internetRepo;
    private final IntranetAppealRepository intranetRepo;

    public AppealService(InternetAppealRepository internetRepo, IntranetAppealRepository intranetRepo) {
        this.internetRepo = internetRepo;
        this.intranetRepo = intranetRepo;
    }

    @Transactional("internetTransactionManager")
    public InternetAppeal submitAppeal(InternetAppeal appeal) {
        appeal.setSubmittedAt(LocalDateTime.now());
        return internetRepo.save(appeal);
    }

    @Transactional("intranetTransactionManager")
    public List<AppealSummaryDto> getStaffAppeals() {
        List<IntranetAppeal> intranetAppeals = intranetRepo.findAllByOrderByLastUpdatedDesc();

        List<Long> ids = intranetAppeals.stream()
                .map(IntranetAppeal::getId)
                .collect(Collectors.toList());

        List<InternetAppeal> internetAppeals = internetRepo.findAllById(ids);
        Map<Long, InternetAppeal> internetMap = internetAppeals.stream()
                .collect(Collectors.toMap(InternetAppeal::getId, Function.identity()));

        return intranetAppeals.stream()
                .map(intranet -> {
                    InternetAppeal internet = internetMap.get(intranet.getId());
                    AppealSummaryDto dto = new AppealSummaryDto();
                    dto.setId(intranet.getId());
                    dto.setCustomerName(internet != null ? internet.getCustomerName() : intranet.getCustomerName());
                    dto.setSubject(internet != null ? internet.getSubject() : intranet.getSubject());
                    dto.setStatus(intranet.getStatus());
                    dto.setLastUpdated(intranet.getLastUpdated());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional("intranetTransactionManager")
    public AppealDetailDto getStaffAppealDetail(Long id) {
        IntranetAppeal intranet = intranetRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appeal not found"));

        InternetAppeal internet = internetRepo.findById(id).orElse(null);

        AppealDetailDto dto = new AppealDetailDto();
        dto.setId(intranet.getId());
        dto.setCustomerName(internet != null ? internet.getCustomerName() : intranet.getCustomerName());
        dto.setSubject(internet != null ? internet.getSubject() : intranet.getSubject());
        dto.setMessage(internet != null ? internet.getMessage() : intranet.getMessage());
        dto.setSubmittedAt(internet != null ? internet.getSubmittedAt() : intranet.getSubmittedAt());
        dto.setStatus(intranet.getStatus());
        dto.setOfficerResponse(intranet.getOfficerResponse());
        dto.setRespondedAt(intranet.getRespondedAt());
        dto.setClosedAt(intranet.getClosedAt());
        dto.setCloseReason(intranet.getCloseReason());
        dto.setLastUpdated(intranet.getLastUpdated());

        return dto;
    }

    @Transactional("intranetTransactionManager")
    public IntranetAppeal respondToAppeal(Long id, String response) {
        IntranetAppeal intranet = intranetRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appeal not found"));

        if (intranet.getStatus() == AppealStatus.CLOSED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Appeal is already CLOSED");
        }

        intranet.setStatus(AppealStatus.AWAITING_CUSTOMER);
        intranet.setOfficerResponse(response);
        intranet.setRespondedAt(LocalDateTime.now());
        intranet.setLastUpdated(LocalDateTime.now());

        return intranetRepo.save(intranet);
    }
}
