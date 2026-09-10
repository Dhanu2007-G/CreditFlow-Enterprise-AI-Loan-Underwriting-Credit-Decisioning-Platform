package com.loanunderwriting.application.repository;

import com.loanunderwriting.application.model.ApplicationStatus;
import com.loanunderwriting.application.model.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, String> {

    List<LoanApplication> findByApplicantId(String applicantId);

    List<LoanApplication> findByStatus(ApplicationStatus status);

    List<LoanApplication> findByEmail(String email);
}