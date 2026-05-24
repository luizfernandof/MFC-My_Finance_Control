package br.com.devl.mfc.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.devl.mfc.entity.TransactionGroup;

public interface TransactionGroupRepository extends JpaRepository<TransactionGroup, UUID> {
}