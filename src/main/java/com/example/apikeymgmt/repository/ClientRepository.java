package com.example.apikeymgmt.repository;

import com.example.apikeymgmt.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, String> {
} 