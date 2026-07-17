package com.example.appeal.controller;

import com.example.appeal.internet.InternetAppeal;
import com.example.appeal.service.AppealService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/appeals")
@CrossOrigin(origins = "*")
public class PublicAppealController {

    private final AppealService appealService;

    public PublicAppealController(AppealService appealService) {
        this.appealService = appealService;
    }

    @PostMapping
    public ResponseEntity<?> submitAppeal(@Valid @RequestBody InternetAppeal appeal) {
        InternetAppeal savedAppeal = appealService.submitAppeal(appeal);
        Map<String, Object> response = Map.of(
                "referenceId", savedAppeal.getId(),
                "message", "Appeal submitted successfully."
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
}
