package com.example.userservice5.repository;
import com.example.userservice5.entity.PitchEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PitchRepository extends JpaRepository<PitchEntity, Long> {
    Optional<PitchEntity> findByIdAndDeletedAtIsNull(Long id);
    PitchEntity findByNameAndDeletedAtIsNull(String name);
    Page<PitchEntity> findByUserDetailIdAndDeletedAtIsNull(Long id, Pageable pageable);
    List<PitchEntity> findByDeletedAtIsNull();
}
