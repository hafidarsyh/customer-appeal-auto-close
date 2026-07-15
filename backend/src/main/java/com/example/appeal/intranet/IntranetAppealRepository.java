package com.example.appeal.intranet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IntranetAppealRepository extends JpaRepository<IntranetAppeal, Long> {

    List<IntranetAppeal> findAllByOrderByLastUpdatedDesc();

    List<IntranetAppeal> findByStatusAndRespondedAtBefore(AppealStatus status, LocalDateTime dateTime);
}
