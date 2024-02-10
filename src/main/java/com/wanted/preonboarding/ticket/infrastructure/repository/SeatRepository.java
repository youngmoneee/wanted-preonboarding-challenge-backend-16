package com.wanted.preonboarding.ticket.infrastructure.repository;

import com.wanted.preonboarding.ticket.domain.entity.SeatInfo;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<SeatInfo, Integer> {
  List<SeatInfo> findAllById(int id);
  Optional<SeatInfo> findSeatInfoByLineAndSeatAndPerformanceId(String line, int seat, UUID performanceId);
}
