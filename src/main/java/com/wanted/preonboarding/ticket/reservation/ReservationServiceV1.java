package com.wanted.preonboarding.ticket.reservation;

import com.wanted.preonboarding.domain.exception.PaymentException;
import com.wanted.preonboarding.domain.exception.ReserveConflictException;
import com.wanted.preonboarding.domain.exception.SeatNotFoundException;
import com.wanted.preonboarding.domain.exception.UserNotFoundException;
import com.wanted.preonboarding.ticket.domain.dto.UserDto;
import com.wanted.preonboarding.ticket.domain.dto.reservation.CreateReservationDto;
import com.wanted.preonboarding.ticket.domain.dto.reservation.ReserveResponseDto;
import com.wanted.preonboarding.ticket.domain.dto.reservation.ReservedListDto;
import com.wanted.preonboarding.ticket.domain.entity.Reservation;
import com.wanted.preonboarding.ticket.domain.entity.SeatInfo;
import com.wanted.preonboarding.ticket.domain.entity.UserInfo;
import com.wanted.preonboarding.ticket.infrastructure.repository.ReservationRepository;
import com.wanted.preonboarding.ticket.infrastructure.repository.SeatRepository;
import com.wanted.preonboarding.ticket.infrastructure.repository.UserInfoRepository;
import com.wanted.preonboarding.ticket.notification.NotificationService;
import com.wanted.preonboarding.ticket.reservation.discount.DiscountPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationServiceV1 implements ReservationService {
  final private ReservationRepository reservationRepository;
  final private SeatRepository        seatRepository;
  final private UserInfoRepository    userInfoRepository;
  final private NotificationService   notificationService;

  @Override
  public ReservedListDto reservedList(UserDto user) {
    ReservedListDto res = ReservedListDto.of(
        this.userInfoRepository.findUserInfoByUserNameAndPhoneNumber(
            user.getUserName(),
            user.getPhoneNumber())
            .orElseThrow(UserNotFoundException::new)
    );
    return res;
  }

  //  할인 정책이 있을 경우, 해당 메소드 호출
  @Override
  @Transactional
  public ReserveResponseDto createReservation(CreateReservationDto createReservationDto,
      DiscountPolicy discountPolicy) {
    UserInfo user = this.userInfoRepository.findUserInfoByUserNameAndPhoneNumber(
        createReservationDto.getName(),
        createReservationDto.getPhone()
    ).orElseThrow(() -> new UserNotFoundException("존재하지 않는 유저"));
    SeatInfo seat = this.seatRepository.findSeatInfoByLineAndSeatAndPerformanceId(
        createReservationDto.getSeatInfo().getLine(),
        createReservationDto.getSeatInfo().getSeat(),
        createReservationDto.getSeatInfo().getPerformanceId()
    ).orElseThrow(() -> new SeatNotFoundException("자리 없음"));

    //  1. 계산
    int price = discountPolicy.calc(seat.getPerformance().getPrice());
    if (price > createReservationDto.getAmount()) throw new PaymentException("잔액 부족");

    Reservation res = null;
    //  2. 예약
    try {
      res = this.reservationRepository.save(Reservation.of(user, seat));
    } catch (Throwable e) {
      throw new ReserveConflictException("이미 예약된 좌석", e);
    }
    //  3. 결제

    //  4. 자리 예약 상태 변경
    seat.setIsReserve("disable");
    this.seatRepository.save(seat);
    return ReserveResponseDto.of(res);
  }

  @Override
  public boolean deleteReservation(int id) {
    Reservation reservation = this.reservationRepository.findById(id).orElseThrow(SeatNotFoundException::new);
    SeatInfo seatInfo = reservation.getSeatInfo();
    seatInfo.setIsReserve("enable");
    this.seatRepository.save(seatInfo);
    this.reservationRepository.deleteById(id);
    this.notificationService.notice(seatInfo.getPerformance().getId());
    return true;
  }
}
