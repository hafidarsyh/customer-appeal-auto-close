package com.example.appeal.controller;

import com.example.appeal.service.SyncJob;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/staff/sync")
@CrossOrigin(origins = "*")
public class SyncController {

    private final SyncJob syncJob;

    public SyncController(SyncJob syncJob) {
        this.syncJob = syncJob;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> manualSync() {
        int copied = syncJob.runSync();
        Map<String, Object> response = Map.of(
                "copied", copied,
                "message", "Manual synchronization completed successfully. Copied " + copied + " new appeals."
        );
        return ResponseEntity.ok(response);
    }
}
