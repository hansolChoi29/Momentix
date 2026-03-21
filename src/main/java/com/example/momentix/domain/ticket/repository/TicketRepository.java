package com.example.momentix.domain.ticket.repository;

import com.example.momentix.domain.paymenthistory.entity.PaymentHistory;
import com.example.momentix.domain.ticket.entity.Tickets;
import com.example.momentix.domain.users.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Tickets, Long> {

    List<Tickets> findByUsers_UserIdOrderByTicketIdDesc(Long userId);

    Page<Tickets> findByUsersAndIsDeletedFalse(Users user, Pageable pageable);
    //----------결제---------
    // 결제ID로 링크된 티켓ID 조회
    @Query("select ticke.ticketId from Tickets ticke where ticke.paymentHistory.paymentHistoryId = :paymentId")
    Optional<Long> findIdByPaymentId(@Param("paymentId") Long paymentId);

    // 같은 예약으로 이미 발급된 티켓이 있는지 (결제-티켓 링크 기준)
    @Query("""
            select (count(ticke) > 0)
              from Tickets ticke
              join ticke.paymentHistory paymentHistory
             where paymentHistory.reservationId = :reservationId
            """)
    boolean existsTicketByReservationId(@Param("reservationId") Long reservationId);

    // 결제 확정 시, 티켓에 결제ID 세팅 (FK 주인: 티켓)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Tickets ticke
               set ticke.paymentHistory = :paymentHistory
             where ticke.ticketId = :ticketId
               and ticke.paymentHistory is null
            """)
    int linkPayment(@Param("ticketId") Long ticketId,
                    @Param("paymentHistory") PaymentHistory paymentHistory);

    @Query("""
    SELECT COUNT(t) > 0
    FROM Tickets t
    JOIN EventTimes et ON et.id = t.eventTimeId
    WHERE t.users.userId = :userId
    AND et.events.id = :eventId
    AND t.isDeleted = false
    """)
    boolean existsByUserIdAndEventId(
            @Param("userId") Long userId,
            @Param("eventId") Long eventId
    );

    //결제 취소
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Tickets tickets
               set tickets.paymentHistory = null
             where tickets.ticketId = :ticketId
               and tickets.paymentHistory.paymentHistoryId = :paymentHistoryId
            """)
    int unlinkPayment(@Param("ticketId") Long ticketId,
                      @Param("paymentHistoryId") Long paymentHistoryId);

}
