package com.example.appeal.controller;

import com.example.appeal.internet.InternetAppeal;
import com.example.appeal.service.AppealService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/appeals")
@CrossOrigin(origins = "*")
public class PublicAppealController {

    private final AppealService appealService;

    public PublicAppealController(AppealService appealService) {
        this.appealService = appealService;
    }

    @PostMapping
    public ResponseEntity<InternetAppeal> submitAppeal(@RequestBody InternetAppeal appeal) {
        InternetAppeal savedAppeal = appealService.submitAppeal(appeal);
        return new ResponseEntity<>(savedAppeal, HttpStatus.CREATED);
    }
}
