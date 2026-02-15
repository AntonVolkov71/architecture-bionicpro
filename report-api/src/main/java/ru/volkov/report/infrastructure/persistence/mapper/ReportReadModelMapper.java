package ru.volkov.report.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.volkov.report.application.readmodel.ReportReadModel;
import ru.volkov.report.infrastructure.persistence.entity.ReportEntity;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ReportReadModelMapper {

    List<ReportReadModel> toReadModelList(List<ReportEntity> entities);
}