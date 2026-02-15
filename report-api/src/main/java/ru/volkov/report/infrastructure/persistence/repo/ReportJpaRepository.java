package ru.volkov.report.infrastructure.persistence.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.volkov.report.infrastructure.persistence.entity.ReportEntity;

@Repository
public interface ReportJpaRepository extends JpaRepository<ReportEntity, Long>, JpaSpecificationExecutor<ReportEntity> {
}
