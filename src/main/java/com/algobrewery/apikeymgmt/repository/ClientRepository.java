package com.algobrewery.apikeymgmt.repository;

import com.algobrewery.apikeymgmt.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, String> {
    Optional<Client> findByClientNameAndOrganizationUuid(String clientName, String organizationUuid);
} 