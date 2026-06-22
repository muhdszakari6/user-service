package com.example.userservice5.repository;

import com.example.userservice5.entity.BookingEntity;
import com.example.userservice5.entity.SessionEntity;
import com.example.userservice5.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<BookingEntity, Long>, JpaSpecificationExecutor<BookingEntity> {
    Optional<BookingEntity> findBySessionAndBookingDateAndDeletedAtIsNull(SessionEntity session, LocalDate bookingDate);

    @Query(value = "SELECT b FROM BookingEntity  b " +
            "LEFT JOIN FETCH b.session s " +
            "LEFT JOIN s.pitch p " +
            "WHERE (p.id = :pitchId) " +
            "AND (b.bookingDate = :bookingDate) " +
            "AND (b.status = 1) " +
            "AND (b.deletedAt IS NULL) " +
            "ORDER BY b.createdAt DESC")
    List<BookingEntity> findBookingEntitiesByPitchAndDate(@Param("pitchId") Long pitchId, @Param("bookingDate") LocalDate bookingDate);

    @Query(value = "SELECT b FROM BookingEntity b " +
            "LEFT JOIN FETCH b.session s " +
            "LEFT JOIN FETCH s.pitch p " +
            "WHERE (:userId IS NULL OR b.user.id = :userId) " +
            "AND (:bookingDate IS NULL OR b.bookingDate = :bookingDate) " +
            "AND (:pitchId IS NULL OR p.id = :pitchId) " +
            "AND (:status IS NULL OR b.status = :status) " +
            "AND ((:deleted = true AND b.deletedAt IS NOT NULL) OR ((:deleted = false OR :deleted IS NULL) AND b.deletedAt IS NULL)) " +
            "ORDER BY b.createdAt DESC",
            countQuery = "SELECT COUNT(b) FROM BookingEntity b " +
                    "LEFT JOIN b.session s " +
                    "LEFT JOIN s.pitch p " +
                    "WHERE (:userId IS NULL OR b.user.id = :userId) " +
                    "AND (:bookingDate IS NULL OR b.bookingDate = :bookingDate) " +
                    "AND (:status IS NULL OR b.status = :status) " +
                    "AND ((:deleted = true AND b.deletedAt IS NOT NULL) OR ((:deleted = false OR :deleted IS NULL) AND b.deletedAt IS NULL)) " +
                    "AND (:pitchId IS NULL OR p.id = :pitchId) "
    )
    Page<BookingEntity> findBookings(
            @Param("userId") Long userId,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("pitchId") Long pitchId,
            @Param("deleted") Boolean deleted,
            @Param("status") BookingStatus status,
            Pageable pageable
    );

    @Query(value = "SELECT b FROM BookingEntity b " +
            "LEFT JOIN FETCH b.session s " +
            "LEFT JOIN FETCH s.pitch p " +
            "WHERE (:userId IS NULL OR b.user.id = :userId) " +
            "AND (:bookingDate IS NULL OR b.bookingDate = :bookingDate) " +
            "AND (:pitchId IS NULL OR p.id = :pitchId) " +
            "AND (:status IS NULL OR b.status = :status) " +
            "AND (p.userDetail.id = :partnerId) " +
            "AND ((:deleted = true AND b.deletedAt IS NOT NULL) OR ((:deleted = false OR :deleted IS NULL) AND b.deletedAt IS NULL)) " +
            "ORDER BY b.createdAt DESC",
            countQuery = "SELECT COUNT(b) FROM BookingEntity b " +
                    "LEFT JOIN b.session s " +
                    "LEFT JOIN s.pitch p " +
                    "WHERE (:userId IS NULL OR b.user.id = :userId) " +
                    "AND (:bookingDate IS NULL OR b.bookingDate = :bookingDate) " +
                    "AND (:pitchId IS NULL OR p.id = :pitchId) " +
                    "AND (:status IS NULL OR b.status = :status) " +
                    "AND ((:deleted = true AND b.deletedAt IS NOT NULL) OR ((:deleted = false OR :deleted IS NULL) AND b.deletedAt IS NULL)) " +
                    "AND (p.userDetail.id = :partnerId) "
    )
    Page<BookingEntity> findPartnerBookings(
            @Param("userId") Long userId,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("pitchId") Long pitchId,
            Pageable pageable,
            @Param("partnerId") Long partnerId,
            @Param("deleted") Boolean deleted,
            @Param("status") BookingStatus status
    );


    @Query(value = "SELECT b FROM BookingEntity b " +
            "LEFT JOIN FETCH b.session s " +
            "LEFT JOIN FETCH s.pitch p " +
            "WHERE (:userId IS NULL OR b.user.id = :userId) " +
            "AND (:bookingDate IS NULL OR b.bookingDate = :bookingDate) " +
            "AND (:pitchId IS NULL OR p.id = :pitchId) " +
            "AND (:status IS NULL OR b.status = :status) " +
            "AND (b.deletedAt IS NULL) " +
            "ORDER BY b.createdAt DESC",
            countQuery = "SELECT COUNT(b) FROM BookingEntity b " +
                    "LEFT JOIN b.session s " +
                    "LEFT JOIN s.pitch p " +
                    "WHERE (:userId IS NULL OR b.user.id = :userId) " +
                    "AND (:bookingDate IS NULL OR b.bookingDate = :bookingDate) " +
                    "AND (:pitchId IS NULL OR p.id = :pitchId) " +
                    "AND (:status IS NULL OR b.status = :status) " +
                    "AND (b.deletedAt IS NULL) "
    )
    Page<BookingEntity> findUserBookings(
            @Param("userId") Long userId,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("pitchId") Long pitchId,
            @Param("status") BookingStatus bookingStatus,
            Pageable pageable
    );
}