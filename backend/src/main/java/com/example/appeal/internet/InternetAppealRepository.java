package com.example.appeal.internet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InternetAppealRepository extends JpaRepository<InternetAppeal, Long> {
}
