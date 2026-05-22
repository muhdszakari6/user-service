package com.example.userservice5.repository;

import com.example.userservice5.entity.BookingEntity;
import com.example.userservice5.entity.SessionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<BookingEntity, Long>, JpaSpecificationExecutor<BookingEntity> {
    Optional<BookingEntity> findBySessionAndBookingDateAndDeletedAtIsNull(SessionEntity session, LocalDate bookingDate);

    @Query(value = "SELECT b FROM BookingEntity b " +
            "LEFT JOIN FETCH b.session s " +
            "LEFT JOIN FETCH s.pitch p " +
            "WHERE (:userId IS NULL OR b.user.id = :userId) " +
            "AND (:bookingDate IS NULL OR b.bookingDate = :bookingDate) " +
            "AND (:pitchId IS NULL OR p.id = :pitchId) " +
            "ORDER BY b.createdAt DESC",
            countQuery = "SELECT COUNT(b) FROM BookingEntity b " +
                    "LEFT JOIN b.session s " +
                    "LEFT JOIN s.pitch p " +
                    "WHERE (:userId IS NULL OR b.user.id = :userId) " +
                    "AND (:bookingDate IS NULL OR b.bookingDate = :bookingDate) " +
                    "AND (:pitchId IS NULL OR p.id = :pitchId) "
    )
    Page<BookingEntity> findBookings(
            @Param("userId") Long userId,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("pitchId") Long pitchId,
            Pageable pageable
    );

    @Query(value = "SELECT b FROM BookingEntity b " +
            "LEFT JOIN FETCH b.session s " +
            "LEFT JOIN FETCH s.pitch p " +
            "WHERE (:userId IS NULL OR b.user.id = :userId) " +
            "AND (:bookingDate IS NULL OR b.bookingDate = :bookingDate) " +
            "AND (:pitchId IS NULL OR p.id = :pitchId) " +
            "AND (p.userDetail.id = :partnerId) " +
            "ORDER BY b.createdAt DESC",
            countQuery = "SELECT COUNT(b) FROM BookingEntity b " +
                    "LEFT JOIN b.session s " +
                    "LEFT JOIN s.pitch p " +
                    "WHERE (:userId IS NULL OR b.user.id = :userId) " +
                    "AND (:bookingDate IS NULL OR b.bookingDate = :bookingDate) " +
                    "AND (:pitchId IS NULL OR p.id = :pitchId) " +
                    "AND (p.userDetail.id = :partnerId) "
    )
    Page<BookingEntity> findPartnerBookings(
            @Param("userId") Long userId,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("pitchId") Long pitchId,
            Pageable pageable,
            @Param("partnerId") Long partnerId
    );
}
