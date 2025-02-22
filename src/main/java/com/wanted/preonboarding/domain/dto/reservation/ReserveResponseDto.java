package com.wanted.preonboarding.domain.dto.reservation;

import com.wanted.preonboarding.domain.dto.performance.PerformanceSeatDto;
import com.wanted.preonboarding.domain.entity.Reservation;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ReserveResponseDto {
  private long id;
  private PerformanceSeatDto seat;

  public static ReserveResponseDto of(Reservation entity) {
    return ReserveResponseDto.builder()
        .id(entity.getId())
        .seat(PerformanceSeatDto.of(
            entity.getSeatInfo()
        )
        ).build();
  }
}
