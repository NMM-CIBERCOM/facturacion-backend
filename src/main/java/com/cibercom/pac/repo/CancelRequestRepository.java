package com.cibercom.pac.repo;

import com.cibercom.pac.model.CancelRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CancelRequestRepository extends JpaRepository<CancelRequest, Long> {
    Optional<CancelRequest> findTopByUuidOrderByIdDesc(String uuid);
}


