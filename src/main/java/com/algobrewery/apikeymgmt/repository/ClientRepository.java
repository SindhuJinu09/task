package com.algobrewery.apikeymgmt.repository;

import com.algobrewery.apikeymgmt.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, String> {
} 