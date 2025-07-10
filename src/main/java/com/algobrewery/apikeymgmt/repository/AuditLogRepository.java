package com.algobrewery.apikeymgmt.repository;

import com.algobrewery.apikeymgmt.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
} 