package com.example.appeal.controller;

import com.example.appeal.dto.AppealDetailDto;
import com.example.appeal.dto.AppealSummaryDto;
import com.example.appeal.intranet.IntranetAppeal;
import com.example.appeal.service.AppealService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff/appeals")
@CrossOrigin(origins = "*")
public class StaffAppealController {

    private final AppealService appealService;

    public StaffAppealController(AppealService appealService) {
        this.appealService = appealService;
    }

    @GetMapping
    public ResponseEntity<List<AppealSummaryDto>> getAppeals() {
        return ResponseEntity.ok(appealService.getStaffAppeals());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppealDetailDto> getAppealDetail(@PathVariable Long id) {
        return ResponseEntity.ok(appealService.getStaffAppealDetail(id));
    }

    @PostMapping("/{id}/respond")
    public ResponseEntity<IntranetAppeal> respondToAppeal(
            @PathVariable Long id,
            @RequestBody ResponseRequest request) {
        IntranetAppeal updatedAppeal = appealService.respondToAppeal(id, request.getResponse());
        return ResponseEntity.ok(updatedAppeal);
    }

    public static class ResponseRequest {
        private String response;

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }
    }
}
