package com.example.userservice5.entity;

import com.example.userservice5.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "booking",
        uniqueConstraints = { @UniqueConstraint(name = "UniqueSessionAndDate", columnNames = { "session_id", "booking_date" }
        )})
public class BookingEntity implements Serializable {

    private static final long serialVersionUID = 8059871061120284233L;

    @Id
    @Getter
    @Setter
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Setter
    @Column
    private LocalDate bookingDate;

    @Getter
    @Setter
    @Column
    private String userEmail;

    @Getter
    @Setter
    @ManyToOne(optional=true)
    @JoinColumn(name="user_id", nullable = true)
    private UserEntity user;

    @Getter
    @Setter
    @ManyToOne()
    @JoinColumn(name="session_id")
    private SessionEntity session;

    @Getter
    @Setter
    private BookingStatus status;

    @CreationTimestamp
    @Getter
    @Setter(value = AccessLevel.NONE)
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Getter
    @Setter(value = AccessLevel.NONE)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    private LocalDateTime deletedAt;

}
